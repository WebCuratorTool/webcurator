<script setup lang="ts">
import { ref } from "vue";
import { type UseFetchApis, useFetch } from '@/utils/rest.api'
import { formatDatetime } from '@/utils/helper';
import { useTargetAnnotationsDTO, useTargetGeneralDTO } from '@/stores/target';

const rest: UseFetchApis = useFetch()

const targetGeneral = useTargetGeneralDTO();
const targetAnnotations = useTargetAnnotationsDTO();

const loading = ref(false);
const targetInstances = ref([]);

const targetTimeline = ref<any[]>([]);

const fetchTargetInstances = () => {
    const searchParams = {
        filter: { targetId: targetGeneral.id },
        offset: 0,
        limit: 1024
    }

    loading.value = true;

    rest
        .post('target-instances', searchParams, { header: 'X-HTTP-Method-Override', value: 'GET' })
        .then((data: any) => {
            targetInstances.value = data['instances']
            loading.value = false
        })
        .catch((err: any) => {
            console.log(err.message)
            loading.value = false
        })

    targetTimeline.value.push({ date: targetGeneral.creationDate, event: "Target Created", type: 'target' })
    targetAnnotations.targetAnnotations.annotations.forEach(annotation => {
        annotation.type = 'target'
        targetTimeline.value.push(annotation);
    });

    targetInstances.value.forEach((ti: any )=> {
      if (ti.status === 5) {
          targetTimeline.value.push({ })
      }
    })
}


targetTimeline.value.reverse()
    
fetchTargetInstances();

</script>

<template>
 <Timeline :value="targetTimeline">
    <template #content="slotProps">
        <div v-if="slotProps.item.event">
            {{ `${formatDatetime(slotProps.item.date)} ${slotProps.item.event}` }}
        </div>
        <div v-else class="anotation-speech-bubble ml-2 mt-1 mb-2">
            <div>
                {{ `${formatDatetime(slotProps.item.date)} - ${slotProps.item.user}` }}
            </div>
            <div class="mt-2">{{ slotProps.item.note }}</div>
        </div>
    </template>
 </Timeline>
</template>

<style>
.anotation-speech-bubble {
  /* triangle dimension */
  --a: 90deg; /* angle */
  --h: 1em;   /* height */

  --p: 0%;  /* triangle position (0%:left 100%:right) */
  --r: 1.2em; /* the radius */
  --b: 5px; /* border width  */
  --c1: #3F51B5;
  --c2: #fafafa;

  max-width: 50ch;
  padding: 1em;
  border-radius: var(--r)/min(var(--r),var(--p) - var(--h)*tan(var(--a)/2)) var(--r) var(--r) min(var(--r),100% - var(--p) - var(--h)*tan(var(--a)/2));
  clip-path: polygon(0 0,100% 0,100% 100%,0 100%,
    0 min(100%,var(--p) + var(--h)*tan(var(--a)/2)),
    calc(-1*var(--h)) var(--p),
    0 max(0%  ,var(--p) - var(--h)*tan(var(--a)/2)));
  background: var(--c1);
  border-image: conic-gradient(var(--c1) 0 0) fill 0/
    max(0%,var(--p) - var(--h)*tan(var(--a)/2)) var(--r) max(0%,100% - var(--p) - var(--h)*tan(var(--a)/2)) 0/0 0 0 var(--h);
  position: relative;
}
.anotation-speech-bubble:before {
  content: "";
  position: absolute;
  z-index: -1;
  inset: 0;
  padding: var(--b);
  border-radius: inherit;
  clip-path: polygon(0 0,100% 0,100% 100%,0 100%,
    var(--b) min(100% - var(--b),var(--p) + var(--h)*tan(var(--a)/2) - var(--b)*tan(45deg - var(--a)/4)),
    calc(var(--b)/sin(var(--a)/2) - var(--h)) var(--p),
    var(--b) max(       var(--b),var(--p) - var(--h)*tan(var(--a)/2) + var(--b)*tan(45deg - var(--a)/4)));
  background: var(--c2) content-box;
  border-image: conic-gradient(var(--c2) 0 0) fill 0/
    max(var(--b),var(--p) - var(--h)*tan(var(--a)/2)) var(--r) max(var(--b),100% - var(--p) - var(--h)*tan(var(--a)/2)) 0/0 0 0 var(--h);
}
</style>