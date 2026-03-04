<template>
  <div class="not-found-container">
    <div class="not-found-404">404</div>
    <div class="not-found-text">页面走丢啦 😵</div>
    <el-button
        type="primary"
        size="large"
        @click="goBack"
        class="not-found-btn"
    >
      返回首页
    </el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found-container {
  width: 100%;
  height: 96vh;
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.not-found-404 {
  font-size: 12rem;
  font-weight: 700;
  color:#2A5CAA;
  line-height: 1;
  margin-bottom: 20px;
}

.not-found-text {
  font-size: 1.5rem;
  color: #606266;
  margin-bottom: 40px;
}

.not-found-btn {
  padding: 12px 36px;
  font-size: 1rem;
  background-color:#1E90FF;
  border-color: #1E90FF;
  --el-button-hover-bg-color: #446CCF ;
  --el-button-hover-border-color: #446CCF;
}
</style>