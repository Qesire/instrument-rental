<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <h2>乐器管理</h2>
      <el-button type="primary" @click="handleAdd">添加乐器</el-button>
    </div>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="serialNo" label="编号" width="150" />
        <el-table-column prop="barcode" label="条码" width="180" />
        <el-table-column prop="modelName" label="型号" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warehouse" label="仓库" width="120" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该乐器吗？"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑乐器' : '添加乐器'"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="编号" prop="serialNo">
          <el-input v-model="form.serialNo" placeholder="乐器编号" />
        </el-form-item>
        <el-form-item label="条码" prop="barcode">
          <el-input v-model="form.barcode" placeholder="条码" />
        </el-form-item>
        <el-form-item label="型号" prop="modelName">
          <el-input v-model="form.modelName" placeholder="型号名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
            <el-option label="在库" value="IN_STOCK" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="已报废" value="SCRAPPED" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouse">
          <el-input v-model="form.warehouse" placeholder="仓库位置" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确 定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getInstruments, createInstrument, updateInstrument, deleteInstrument
} from '../api'

const tableData = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const editingId = ref<number | null>(null)

const form = reactive({
  serialNo: '',
  barcode: '',
  modelName: '',
  status: 'IN_STOCK',
  warehouse: '',
  remark: ''
})

const rules = {
  serialNo: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  barcode: [{ required: true, message: '请输入条码', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入型号', trigger: 'blur' }]
}

const statusText = (s: string) => {
  const map: Record<string, string> = {
    IN_STOCK: '在库',
    RESERVED: '已预约',
    RENTED: '租赁中',
    MAINTENANCE: '维护中',
    SCRAPPED: '已报废'
  }
  return map[s] ?? s
}

const statusType = (s: string) => {
  const map: Record<string, string> = {
    IN_STOCK: 'success',
    RESERVED: 'warning',
    RENTED: '',
    MAINTENANCE: 'danger',
    SCRAPPED: 'info'
  }
  return map[s] ?? 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getInstruments()
    tableData.value = res.data?.records ?? res.data ?? res
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  editingId.value = null
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editingId.value = row.id
  form.serialNo = row.serialNo ?? ''
  form.barcode = row.barcode ?? ''
  form.modelName = row.modelName ?? ''
  form.status = row.status ?? 'IN_STOCK'
  form.warehouse = row.warehouse ?? ''
  form.remark = row.remark ?? ''
  dialogVisible.value = true
}

async function handleDelete(id: number) {
  try {
    await deleteInstrument(id)
    ElMessage.success('删除成功')
    fetchList()
  } catch { /* handled */ }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && editingId.value) {
      await updateInstrument(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createInstrument({ ...form })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch { /* handled */ }
  finally {
    submitting.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
  form.serialNo = ''
  form.barcode = ''
  form.modelName = ''
  form.status = 'IN_STOCK'
  form.warehouse = ''
  form.remark = ''
}

onMounted(fetchList)
</script>