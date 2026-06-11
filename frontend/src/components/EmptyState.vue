<template>
  <div class="empty-state">
    <div class="empty-icon">
      <slot name="icon">
        <van-icon :name="icon" :size="iconSize" :color="iconColor" />
      </slot>
    </div>
    <div class="empty-text">{{ text }}</div>
    <div class="empty-subtext" v-if="subtext">{{ subtext }}</div>
    <button v-if="buttonText" class="empty-btn" @click="$emit('click')">
      {{ buttonText }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { vanIcon } from 'vant'

defineProps<{
  icon?: string
  iconSize?: number
  iconColor?: string
  text?: string
  subtext?: string
  buttonText?: string
}>()

defineEmits<{
  click: []
}>()
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #F5F5F7 0%, #E8E8ED 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.empty-text {
  font-size: 17px;
  font-weight: 500;
  color: #1D1D1F;
  margin-bottom: 8px;
}

.empty-subtext {
  font-size: 14px;
  color: #86868B;
  margin-bottom: 24px;
}

.empty-btn {
  padding: 12px 32px;
  background: #2B95FF;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(43,149,255,0.3);
}

.empty-btn:active {
  transform: scale(0.98);
}
</style>
