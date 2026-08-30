<script setup lang="ts">
import { computed, useAttrs } from "vue";

const props = defineProps<{
  label?: string;
  icon?: string;
  iconPos?: "left" | "right";
  text?: boolean;
  outlined?: boolean;
  rounded?: boolean;
  severity?: string;
  disabled?: boolean;
  type?: "button" | "submit" | "reset";
  fluid?: boolean;
}>();

defineEmits<{
  click: [event: MouseEvent];
}>();

const attrs = useAttrs();

const variant = computed(() => {
  if (props.text) {
    return "ghost";
  }
  if (props.outlined) {
    return "outline";
  }
  return "solid";
});

const color = computed(() => {
  if (props.severity === "danger") {
    return "error";
  }
  if (props.severity === "secondary") {
    return "neutral";
  }
  return "primary";
});
</script>

<template>
  <UButton
    v-bind="attrs"
    :type="type || 'button'"
    :disabled="disabled"
    :variant="variant"
    :color="color"
    :block="fluid"
    :class="[{ 'rounded-full': rounded }, attrs.class]"
    @click="$emit('click', $event)"
  >
    <template v-if="icon && (!iconPos || iconPos === 'left')" #leading>
      <i :class="icon" />
    </template>
    <slot>{{ label }}</slot>
    <template v-if="icon && iconPos === 'right'" #trailing>
      <i :class="icon" />
    </template>
  </UButton>
</template>
