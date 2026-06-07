<template>
  <div>
    <h2>扫码操作</h2>

    <el-card style="max-width: 600px">
      <div style="margin-bottom: 16px">
        <el-input
          v-model="barcode"
          placeholder="请扫描或输入条码"
          size="large"
          clearable
          @keyup.enter="handleAction('checkout')"
        >
          <template #prefix>
            <el-icon><Scan /></el-icon>
          </template>
        </el-input>
      </div>

      <div style="display: flex; gap: 12px; margin-bottom: 12px; align-items: center">
        <el-button type="primary" :loading="working" @click="handleAction('checkout')">
          取出
        </el-button>
        <el-button type="success" :loading="working" @click="handleAction('checkin')">
          归还
        </el-button>
        <el-checkbox v-model="damaged">损坏</el-checkbox>
      </div>

      <!-- 结果卡片 -->
      <el-alert
        v-if="result"
        :title="result.message"
        :type="result.success ? 'success' : 'error'"
        :closable="false"
        show-icon
      >
        <template v-if="result.success && result.data">
          <p>编号: {{ result.data.serialNo }}</p>
          <p>型号: {{ result.data.modelName }}</p>
          <p>状态: {{ result.data.status }}</p>
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Scan } from '@element-plus/icons-vue'
import { scanCheckout, scanCheckin } from '../api'

const barcode = ref('')
const damaged = ref(false)
const working = ref(false)
const result = ref<any>(null)

async function handleAction(type: 'checkout' | 'checkin') {
  if (!barcode.value.trim()) {
    ElMessage.warning('请先输入条码')
    return
  }

  working.value = true
  result.value = null
  try {
    const apiFn = type === 'checkout' ? scanCheckout : scanCheckin
    const res: any = await apiFn({
      barcode: barcode.value.trim(),
      damaged: damaged.value
    })
    result.value = {
      success: true,
      message: type === 'checkout' ? '取出成功' : '归还成功',
      data: res.data ?? res
    }
    barcode.value = ''
    damaged.value = false
    ElMessage.success(type === 'checkout' ? '取出成功' : '归还成功')
  } catch (e: any) {
    result.value = {
      success: false,
      message: e?.message ?? '操作失败'
    }
  } finally {
    working.value = false
  }
}
</script>