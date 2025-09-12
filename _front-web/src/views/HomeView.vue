<!-- src/views/HomeView.vue -->
<template>
  <main class="home-page">
    <div class="content">
      <h1>Welcome to Tools-AIO</h1>
      <p>这是一个最小首页占位，你可以在这里开始搭建你的界面。</p>
      <n-space>
        <n-button quaternary text class="icon-button">
          <div class="icons">
            <HomeIcon />
            <CashIcon />
          </div>
        </n-button>
      </n-space>
    </div>
  </main>
</template>

<script setup lang="ts">
// 空 - 未来可以放 props / setup 逻辑
import HomeIcon from "@/components/icons/HomeIcon.vue";
import { CashOutline as CashIcon } from '@vicons/ionicons5'
</script>

<style scoped>

.icon-button {
  display: flex;
  justify-content: center;
  align-items: center;
}

.icons {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}
:deep(svg) {
  width: 40px;
  height: 40px;
}
svg {
  width: 40px;
  height: 40px;
  cursor: pointer;
  transition: transform 0.2s ease;
}
.home-page {
  /* 关键：使该组件在 #app 的 flex 容器中占满可用空间 */
  flex: 1;
  min-height: 0; /* 避免在 flex 容器内高度塌陷 */
  display: flex;
  align-items: center;
  justify-content: center;

  /*我觉得很好看的红色 #c81515 */
  background: #272727; /* 黑色背景（可去掉，已在全局设置） */
  color: #fff;
}

.content {
  text-align: center;
  padding: 1rem;
}
</style>
