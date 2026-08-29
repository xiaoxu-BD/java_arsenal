<script setup lang="ts">
import { onMounted, onUnmounted, ref, type Ref } from 'vue';
import {
  downloadTaskFile,
  downloadTemplate,
  generateData,
  getTask,
  startAsyncExport,
  startAsyncImport,
  syncExport,
  syncImport,
  type ExcelTask,
  type ImportResult,
} from './api';

/** 任务列表的展示项: 后端 ExcelTask + 前端自己记的类型标签 */
interface UiTask {
  kind: '导入' | '导出';
  task: ExcelTask;
}

// ---- 0. 造数 ----
const genCount = ref(100000);
const genBusy = ref(false);
const genMessage = ref('');

// ---- 1. 同步接口 ----
const syncFile = ref<File | null>(null);
const syncBusy = ref(false);
const syncError = ref('');
const syncResult = ref<ImportResult | null>(null);

// ---- 2. 异步接口 ----
const asyncFile = ref<File | null>(null);
const tasks = ref<UiTask[]>([]);

let timer: number | undefined;

onMounted(() => {
  // 每秒刷新一次还在跑的任务, DONE/FAILED 后自然停止更新
  timer = window.setInterval(refreshRunningTasks, 1000);
});

onUnmounted(() => window.clearInterval(timer));

async function refreshRunningTasks(): Promise<void> {
  for (const t of tasks.value) {
    if (t.task.status === 'RUNNING') {
      t.task = await getTask(t.task.id);
    }
  }
}

function pickFile(target: 'sync' | 'async', event: Event): void {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0] ?? null;
  if (target === 'sync') {
    syncFile.value = file;
  } else {
    asyncFile.value = file;
  }
  // 清掉 value, 否则第二次选同一个文件不触发 change
  input.value = '';
}

async function run(busy: Ref<boolean>, error: Ref<string>, action: () => Promise<void>): Promise<void> {
  busy.value = true;
  error.value = '';
  try {
    await action();
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    busy.value = false;
  }
}

function generate(): Promise<void> {
  return run(genBusy, genMessage, async () => {
    const created = await generateData(genCount.value);
    genMessage.value = `已插入 ${created} 行`;
  });
}

function doSyncImport(): Promise<void> {
  const file = syncFile.value;
  if (!file) {
    return Promise.resolve();
  }
  return run(syncBusy, syncError, async () => {
    syncResult.value = await syncImport(file);
  });
}

function doAsyncImport(): Promise<void> {
  const file = asyncFile.value;
  if (!file) {
    return Promise.resolve();
  }
  return run(syncBusy, syncError, async () => {
    const task = await startAsyncImport(file);
    tasks.value.unshift({ kind: '导入', task });
    asyncFile.value = null;
  });
}

function doAsyncExport(): Promise<void> {
  return run(syncBusy, syncError, async () => {
    const task = await startAsyncExport();
    tasks.value.unshift({ kind: '导出', task });
  });
}

/** 导出任务有 total 能算百分比; 导入读不到总数, 只展示已处理行数 */
function percent(task: ExcelTask): number {
  return task.total > 0 ? Math.floor((task.processed / task.total) * 100) : 0;
}
</script>

<template>
  <h1>
    Excel 导入导出 Demo
    <span class="sub">Apache Fesod 2.0.2 · 同步小表 + 异步大表 + 三层校验</span>
  </h1>

  <section class="card">
    <h2>0. 数据准备</h2>
    <div class="row">
      <input v-model.number="genCount" type="number" min="1" max="1000000" />
      <button :disabled="genBusy" @click="generate">造随机用户</button>
      <span class="msg">{{ genMessage }}</span>
    </div>
  </section>

  <section class="card">
    <h2>1. 同步接口（小表，一次请求跑完）</h2>
    <div class="row">
      <button @click="downloadTemplate">下载导入模板</button>
      <button @click="syncExport">同步导出</button>
    </div>
    <div class="row">
      <input type="file" accept=".xlsx" @change="pickFile('sync', $event)" />
      <button :disabled="!syncFile || syncBusy" @click="doSyncImport">同步导入</button>
      <span v-if="syncFile" class="msg">已选: {{ syncFile.name }}</span>
    </div>
    <p v-if="syncError" class="err">{{ syncError }}</p>
    <template v-if="syncResult">
      <p>
        共 {{ syncResult.total }} 行：成功 <b class="ok">{{ syncResult.successCount }}</b>，失败
        <b class="bad">{{ syncResult.failCount }}</b>
      </p>
      <div v-if="syncResult.errors.length" class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>行号</th>
              <th>姓名</th>
              <th>手机号</th>
              <th>错误原因</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(e, i) in syncResult.errors" :key="i">
              <td>{{ e.rowIndex }}</td>
              <td>{{ e.name ?? '-' }}</td>
              <td>{{ e.phone ?? '-' }}</td>
              <td>{{ e.reason }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </section>

  <section class="card">
    <h2>2. 异步接口（大表，提交返回 taskId + 轮询）</h2>
    <div class="row">
      <input type="file" accept=".xlsx" @change="pickFile('async', $event)" />
      <button :disabled="!asyncFile || syncBusy" @click="doAsyncImport">异步导入</button>
      <button :disabled="syncBusy" @click="doAsyncExport">异步导出</button>
      <span v-if="asyncFile" class="msg">已选: {{ asyncFile.name }}</span>
    </div>

    <div v-for="t in tasks" :key="t.task.id" class="task">
      <div class="row" style="margin-bottom: 0">
        <b>{{ t.kind }}</b>
        <span>{{ t.task.id }}</span>
        <span class="badge" :class="t.task.status">{{ t.task.status }}</span>
        <button v-if="t.task.status === 'DONE' && t.task.filePath" @click="downloadTaskFile(t.task.id)">
          {{ t.task.type === 'IMPORT' ? '下载错误回执' : '下载导出文件' }}
        </button>
      </div>
      <template v-if="t.task.type === 'EXPORT' && t.task.total > 0">
        <div class="bar"><div :style="{ width: percent(t.task) + '%' }"></div></div>
        <span class="counts">{{ t.task.processed }} / {{ t.task.total }}（{{ percent(t.task) }}%）</span>
      </template>
      <p v-else class="counts" style="margin: 6px 0 0">已处理 {{ t.task.processed }} 行</p>
      <p v-if="t.task.type === 'IMPORT'" class="counts" style="margin: 4px 0 0">
        成功 <b class="ok">{{ t.task.successCount }}</b> · 失败 <b class="bad">{{ t.task.failCount }}</b>
      </p>
      <p v-if="t.task.errorMsg" class="err" style="margin: 4px 0 0">{{ t.task.errorMsg }}</p>
    </div>
  </section>
</template>
