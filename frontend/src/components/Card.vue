<template>
  <div class="card" :class="[`card-${size}`, { 'card-hover': hover }]" @click="$emit('click')">
    <div class="card-header" v-if="$slots.header">
      <slot name="header"></slot>
    </div>
    <div class="card-body">
      <slot></slot>
    </div>
    <div class="card-footer" v-if="$slots.footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  size?: 'small' | 'medium' | 'large'
  hover?: boolean
}>()

defineEmits<{
  click: []
}>()
</script>

<style scoped>
.card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: all 0.2s ease;
}

.card-small {
  padding: 12px;
}

.card-medium {
  padding: 16px;
}

.card-large {
  padding: 20px;
}

.card-hover {
  cursor: pointer;
}

.card-hover:active {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.card-header {
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid #F5F5F7;
}

.card-footer {
  padding-top: 12px;
  margin-top: 12px;
  border-top: 1px solid #F5F5F7;
}
</style>
