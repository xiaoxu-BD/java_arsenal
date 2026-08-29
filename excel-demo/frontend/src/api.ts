/**
 * 后端接口封装。JSON 类接口走 api<T>, 下载类接口拿 blob + Content-Disposition 文件名后触发保存。
 * 类型与后端 dto 一一对应, 字段含义见后端 README。
 */

export interface ImportErrorRow {
  rowIndex: number | null;
  name: string | null;
  phone: string | null;
  email: string | null;
  deptName: string | null;
  salary: number | null;
  hireDate: string | null;
  reason: string;
}

export interface ImportResult {
  total: number;
  successCount: number;
  failCount: number;
  errors: ImportErrorRow[];
}

export interface ExcelTask {
  id: string;
  type: 'IMPORT' | 'EXPORT';
  status: 'RUNNING' | 'DONE' | 'FAILED';
  total: number;
  processed: number;
  successCount: number;
  failCount: number;
  filePath: string | null;
  errorMsg: string | null;
  createTime: string;
}

async function errorDetail(resp: Response): Promise<string> {
  // 后端错误统一是 ProblemDetail {detail: "..."}, 拿不到就退回状态码
  try {
    const problem = await resp.json();
    return problem.detail ?? JSON.stringify(problem);
  } catch {
    return `HTTP ${resp.status}`;
  }
}

async function api<T>(url: string, init?: RequestInit): Promise<T> {
  const resp = await fetch(url, init);
  if (!resp.ok) {
    throw new Error(await errorDetail(resp));
  }
  return resp.json() as Promise<T>;
}

function uploadForm(file: File): FormData {
  const form = new FormData();
  form.append('file', file);
  return form;
}

async function download(url: string): Promise<void> {
  const resp = await fetch(url);
  if (!resp.ok) {
    throw new Error(await errorDetail(resp));
  }
  // 后端统一用 RFC 5987 写法: filename*=UTF-8''xxx.xlsx
  const disposition = resp.headers.get('Content-Disposition') ?? '';
  const match = disposition.match(/filename\*=UTF-8''([^;]+)/i);
  const fileName = match ? decodeURIComponent(match[1]) : 'download.xlsx';
  const blob = await resp.blob();
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = fileName;
  a.click();
  URL.revokeObjectURL(a.href);
}

export function generateData(count: number): Promise<number> {
  return api<number>(`/api/data/generate?count=${count}`, { method: 'POST' });
}

export function downloadTemplate(): Promise<void> {
  return download('/api/excel/template');
}

export function syncExport(): Promise<void> {
  return download('/api/excel/export');
}

export function syncImport(file: File): Promise<ImportResult> {
  return api<ImportResult>('/api/excel/import', { method: 'POST', body: uploadForm(file) });
}

export function startAsyncImport(file: File): Promise<ExcelTask> {
  return api<ExcelTask>('/api/excel/async/import', { method: 'POST', body: uploadForm(file) });
}

export function startAsyncExport(): Promise<ExcelTask> {
  return api<ExcelTask>('/api/excel/async/export', { method: 'POST' });
}

export function getTask(id: string): Promise<ExcelTask> {
  return api<ExcelTask>(`/api/excel/async/${id}`);
}

export function downloadTaskFile(id: string): Promise<void> {
  return download(`/api/excel/async/${id}/file`);
}
