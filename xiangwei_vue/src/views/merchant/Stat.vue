<template>
  <div class="stats-container">
    <el-row :gutter="20">
      <el-col :span="11">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><List /></el-icon> 近期销售明细</span>
            </div>
          </template>

          <el-table :data="dailyData" stripe border height="450" v-loading="loading">
            <el-table-column prop="date" label="销售日期" align="center" width="120" />
            <el-table-column prop="orderCount" label="订单数" align="center" />
            <el-table-column prop="totalAmount" label="销售额(¥)" align="center">
              <template #default="{ row }">
                <span style="color: #67c23a; font-weight: bold">
                  {{ Number(row.totalAmount || 0).toFixed(2) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="13">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><PieChart /></el-icon> 商品销量占比</span>
            </div>
          </template>

          <div ref="pieRef" class="chart-box"></div>
          <el-empty v-if="!loading && !hasData" description="暂无销量数据" :image-size="100" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { List, PieChart } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getAllStats as getAllStatsAPI } from '@/api/statistics'

const dailyData = ref([])
const loading = ref(false)
const hasData = ref(true)
const pieRef = ref(null)

let myChart = null

const resizeHandler = () => {
  if (myChart) myChart.resize()
}

const unwrap = (res) => (res && res.data ? res.data : res)

const normalizePie = (arr = []) =>
  arr
    .map((it) => ({
      name: it?.name ?? it?.productName ?? it?.productname ?? it?.title ?? '未知商品',
      value: Number(it?.value ?? it?.count ?? it?.num ?? it?.total ?? it?.sales ?? 0)
    }))
    .filter((it) => it.name && it.value > 0)

const initChart = (data) => {
  if (!pieRef.value) return

  if (myChart) myChart.dispose()
  myChart = echarts.init(pieRef.value)

  myChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b} : {c} 元({d}%)' },
    legend: { orient: 'horizontal', bottom: 'bottom' },
    series: [
      {
        name: '商品销量',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}' },
        data
      }
    ]
  })
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllStatsAPI()
    const data = unwrap(res)

    dailyData.value = Array.isArray(data?.daily) ? data.daily : []

    const pie = normalizePie(Array.isArray(data?.pie) ? data.pie : [])
    if (pie.length > 0) {
      hasData.value = true
      await nextTick()
      initChart(pie)
    } else {
      hasData.value = false
      if (myChart) {
        myChart.dispose()
        myChart = null
      }
    }
  } catch (error) {
    console.error('加载统计失败:', error)
    ElMessage.error(error?.message || '获取统计数据失败')
    hasData.value = false
    dailyData.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', resizeHandler)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeHandler)
  if (myChart) myChart.dispose()
  myChart = null
})
</script>

<style scoped>
.stats-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.card-header .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.chart-box {
  width: 100%;
  height: 450px;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  background-color: #fafafa;
}
</style>
