import { toRefs, reactive, computed } from 'vue';

const layoutConfig = reactive({
    darkTheme: false,
    theme: 'md-light-indigo',
});


export function useLayout() {
    const isDarkTheme = computed(() => layoutConfig.darkTheme);

    return { layoutConfig: toRefs(layoutConfig), isDarkTheme };
}
