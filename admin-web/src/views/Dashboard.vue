<template>
  <div>
    <h2>库存看板</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="在库" :value="dashboard.inStock ?? 0">
            <template #prefix>
              <el-icon color="#67c23a"><CircleCheck /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="已预约" :value="dashboard.reserved ?? 0">
            <template #prefix>
              <el-icon color="#e6a23c"><Clock /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="租赁中" :value="dashboard.rented ?? 0">
            <template #prefix>
              <el-icon color="#409eff"><VideoPlay /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="维护中" :value="dashboard.maintenance ?? 0">
            <template #prefix>
              <el-icon color="#f56c6c"><WarningFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 乐器列表 -->
    <el-card>
      <template #header>
        <span>乐器列表</span>
      </template>
      <el-table :data="instruments" v-loading="loading" stripe>
        <el-table-column prop="serialNo" label="编号" width="150" />
        <el-table-column prop="barcode" label="条码" width="180" />
        <el-table-column prop="modelName" label="型号" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warehouse" label="仓库" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { CircleCheck, Clock, VideoPlay, WarningFilled } from '@element-plus/icons-vue'
import { getDashboard, getInstruments } from '../api'

const dashboard = ref<any>({})
const instruments = ref<any[]>([])
const loading = ref(false)

const statusLabel = (status: string) => {
  const map: Record<string, string> = {
    IN_STOCK: '在库',
    RESERVED: '已预约',
    RENTED: '租赁中',
    MAINTENANCE: '维护中',
    SCRAPPED: '已报废'
  }
  return map[status] ?? status
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    IN_STOCK: 'success',
    RESERVED: 'warning',
    RENTED: '',
    MAINTENANCE: 'danger',
    SCRAPPED: 'info'
  }
  return map[status] ?? 'info'
}

onMounted(async () => {
  try {
    const [dashRes, instRes] = await Promise.all([
      getDashboard(),
      getInstruments()
    ])
    dashboard.value = (dashRes as any).data ?? dashRes
    instruments.value = (instRes as any).data ?? instRes
  } catch (e) {
    // handled by interceptor
  }
})
</script>