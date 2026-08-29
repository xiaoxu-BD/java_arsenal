/**
 * 前端只做三件事：切片、传输、展示进度。
 * 分片大小、总片数、已传哪些片（断点续传）、完整性校验、合并时机全部由后端决定。
 */

interface InitResponse {
  uploadId: string;
  chunkSize: number;
  totalChunks: number;
}

interface StatusResponse {
  uploadId: string;
  fileName: string;
  chunkSize: number;
  totalChunks: number;
  uploadedChunks: number[];
  finished: boolean;
  mergedPath?: string;
}

interface MergeResponse {
  fileName: string;
  size: number;
  path: string;
}

/** 同时在传的分片数 */
const CONCURRENCY = 3;

const fileInput = document.querySelector<HTMLInputElement>('#file')!;
const uploadBtn = document.querySelector<HTMLButtonElement>('#upload')!;
const ossToggle = document.querySelector<HTMLInputElement>('#oss-mode')!;
const progressBar = document.querySelector<HTMLElement>('#bar > div')!;
const statusEl = document.querySelector<HTMLElement>('#status')!;

/** 当前请求走本地磁盘版还是 OSS 版后端接口 */
let apiBase = '/api/upload';

function setStatus(text: string): void {
  statusEl.textContent = text;
}

function setProgress(loaded: number, total: number): void {
  progressBar.style.width = total > 0 ? `${((loaded / total) * 100).toFixed(1)}%` : '0';
}

/** 断点续传：同一文件（名字 + 大小）复用上次的 uploadId，本地/OSS 模式分开记 */
function resumeKey(file: File): string {
  return `chunk-upload:${apiBase}:${file.name}:${file.size}`;
}

/** 第 index 片的实际字节数（最后一片可能不满） */
function chunkBytes(file: File, chunkSize: number, index: number): number {
  return Math.min(chunkSize, file.size - index * chunkSize);
}

async function api<T>(url: string, init?: RequestInit): Promise<T> {
  const resp = await fetch(url, init);
  if (!resp.ok) {
    throw new Error(`HTTP ${resp.status}: ${await resp.text()}`);
  }
  return resp.json() as Promise<T>;
}

/** 单分片上传，用 XHR 以便拿到浏览器上传进度 */
function uploadChunk(uploadId: string, index: number, blob: Blob, onProgress: (loaded: number) => void): Promise<void> {
  const form = new FormData();
  form.append('file', blob, `chunk_${index}`);
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    xhr.open('POST', `${apiBase}/${uploadId}/chunk/${index}`);
    xhr.timeout = 120_000;
    xhr.upload.onprogress = e => onProgress(e.loaded);
    xhr.onload = () => xhr.status < 300
      ? resolve()
      : reject(new Error(`分片 ${index} 上传失败: HTTP ${xhr.status} ${xhr.responseText}`));
    xhr.onerror = () => reject(new Error(`分片 ${index} 网络错误(xhr.status=${xhr.status})`));
    xhr.ontimeout = () => reject(new Error(`分片 ${index} 上传超时(120s)`));
    xhr.send(form);
  });
}

/** 偶发网络错误自动重试，后端对同一分片幂等（REPLACE_EXISTING），重传安全 */
async function uploadChunkWithRetry(uploadId: string, index: number, blob: Blob,
                                    onProgress: (loaded: number) => void): Promise<void> {
  const maxAttempts = 3;
  for (let attempt = 1; ; attempt++) {
    try {
      return await uploadChunk(uploadId, index, blob, onProgress);
    } catch (e) {
      if (attempt >= maxAttempts) {
        throw e;
      }
      console.warn(`分片 ${index} 第 ${attempt} 次上传失败，${attempt * 500}ms 后重试:`, e);
      await new Promise(r => setTimeout(r, attempt * 500));
    }
  }
}

/** 固定并发数的任务池 */
async function runPool(items: number[], limit: number, worker: (item: number) => Promise<void>): Promise<void> {
  const queue = [...items];
  await Promise.all(Array.from({ length: Math.min(limit, queue.length) }, async () => {
    for (let item = queue.shift(); item !== undefined; item = queue.shift()) {
      await worker(item);
    }
  }));
}

async function upload(file: File): Promise<void> {
  uploadBtn.disabled = true;
  apiBase = ossToggle.checked ? '/api/oss-upload' : '/api/upload';
  try {
    // 1. 断点续传：本地记着这个文件的任务就先问后端进度；后端若已重启丢任务则重新初始化
    const savedId = localStorage.getItem(resumeKey(file));
    let uploadId: string | null = null;
    let chunkSize = 0;
    let totalChunks = 0;
    const uploaded = new Set<number>();

    if (savedId) {
      const status = await api<StatusResponse>(`${apiBase}/${savedId}/status`).catch(() => undefined);
      if (status?.finished) {
        setStatus(`该文件此前已上传完成：\n${status.mergedPath ?? ''}`);
        return;
      }
      if (status) {
        uploadId = status.uploadId;
        chunkSize = status.chunkSize;
        totalChunks = status.totalChunks;
        for (const i of status.uploadedChunks) uploaded.add(i);
        setStatus(`检测到未完成的上传任务，续传 ${uploaded.size}/${totalChunks} 片...`);
      }
    }
    if (uploadId === null) {
      const init = await api<InitResponse>(`${apiBase}/init`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fileName: file.name, fileSize: file.size }),
      });
      uploadId = init.uploadId;
      chunkSize = init.chunkSize;
      totalChunks = init.totalChunks;
      localStorage.setItem(resumeKey(file), uploadId);
    }
    const id = uploadId;

    // 2. 找出缺失的分片并发上传，进度 = 已完成分片 + 传输中部分
    const pending: number[] = [];
    for (let i = 0; i < totalChunks; i++) {
      if (!uploaded.has(i)) pending.push(i);
    }
    let doneBytes = 0;
    for (const i of uploaded) doneBytes += chunkBytes(file, chunkSize, i);
    setProgress(doneBytes, file.size);

    let doneCount = uploaded.size;
    const inflight = new Map<number, number>();
    await runPool(pending, CONCURRENCY, async index => {
      const blob = file.slice(index * chunkSize, index * chunkSize + chunkBytes(file, chunkSize, index));
      inflight.set(index, 0);
      await uploadChunkWithRetry(id, index, blob, loaded => {
        inflight.set(index, loaded);
        setProgress(doneBytes + [...inflight.values()].reduce((a, b) => a + b, 0), file.size);
      });
      inflight.delete(index);
      doneBytes += blob.size;
      doneCount++;
      setProgress(doneBytes, file.size);
      setStatus(`已上传 ${doneCount} / ${totalChunks} 片`);
    });

    // 3. 分片传齐，交给后端合并并校验
    setStatus('分片传齐，正在合并...');
    const merged = await api<MergeResponse>(`${apiBase}/${id}/merge`, { method: 'POST' });
    setProgress(file.size, file.size);
    setStatus(`上传完成：${merged.fileName}（${merged.size} 字节）\n已保存到后端：${merged.path}`);
    localStorage.removeItem(resumeKey(file));
  } catch (e) {
    setStatus(`上传中断：${(e as Error).message}\n重新点击上传按钮可断点续传。`);
  } finally {
    uploadBtn.disabled = false;
  }
}

fileInput.addEventListener('change', () => {
  uploadBtn.disabled = !fileInput.files?.length;
});

uploadBtn.addEventListener('click', () => {
  const file = fileInput.files?.[0];
  if (file) void upload(file);
});
