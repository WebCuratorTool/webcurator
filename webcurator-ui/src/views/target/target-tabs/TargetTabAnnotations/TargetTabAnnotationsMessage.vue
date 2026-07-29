<script setup lang="ts">
import { reactive, ref } from "vue";
import { useI18n } from "vue-i18n";

import type { Annotation } from "@/types/annotation";
import { formatDate } from "@/utils/helper";

const props = defineProps<{
  annotation: Annotation;
  editing?: boolean;
}>();
const { t } = useI18n();

const editingAnnotation = ref(false);

const localAnnotation = reactive({
  note: props.annotation.note,
  alert: props.annotation.alert,
});

const cancelEditAnnotation = () => {
  localAnnotation.note = props.annotation.note;
  localAnnotation.alert = props.annotation.alert;
  editingAnnotation.value = false;
};
</script>

<template>
  <Card class="mt-3">
    <template #subtitle>
      <div class="flex justify-between items-center">
        <div class="flex justify-between w-full">
          <div class="flex gap-2 items-center">
            <div>{{ annotation.user }} {{ formatDate(annotation.date) }}</div>
            <i
              v-if="annotation.alert && !editingAnnotation"
              class="pi pi-exclamation-triangle"
            />
            <div v-if="editingAnnotation" class="flex items-center gap-2">
              <label>
                <Checkbox v-model="localAnnotation.alert" binary />
                {{ t("target.annotationsPanel.generateAlert") }}
              </label>
            </div>
          </div>
          <div v-if="annotation.targetInstanceId">
            {{ t("target.annotationsPanel.targetInstance") }}
            {{ annotation.targetInstanceId }}
          </div>
        </div>
        <div
          v-if="editing && !annotation.targetInstanceId && !editingAnnotation"
          class="flex"
        >
          <Button
            style="width: 2rem"
            icon="pi pi-trash"
            v-tooltip.bottom="t('target.annotationsPanel.deleteAnnotation')"
            text
            @click="$emit('deleteAnnotation', annotation)"
          />
          <Button
            style="width: 2rem"
            icon="pi pi-pencil"
            v-tooltip.bottom="t('target.annotationsPanel.editAnnotation')"
            text
            @click="editingAnnotation = true"
          />
        </div>
        <div
          v-else-if="
            editing && !annotation.targetInstanceId && editingAnnotation
          "
          class="flex"
        >
          <Button
            class="p-button-text"
            style="width: 2rem"
            icon="pi pi-save"
            v-tooltip.bottom="t('common.save')"
            text
            @click="
              ($emit('saveAnnotation', localAnnotation),
              (editingAnnotation = false))
            "
          />
          <Button
            class="p-button-text"
            style="width: 2rem"
            icon="pi pi-times"
            v-tooltip.bottom="t('common.cancel')"
            text
            @click="cancelEditAnnotation()"
          />
        </div>
      </div>
    </template>
    <template #content>
      <Textarea
        v-if="editing && editingAnnotation && !annotation.targetInstanceId"
        v-model="localAnnotation.note"
        autoResize
        rows="3"
        class="w-full"
      />
      <p v-else>{{ annotation.note }}</p>
    </template>
  </Card>
</template>
