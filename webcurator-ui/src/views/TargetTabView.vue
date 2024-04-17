<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { onBeforeRouteUpdate } from 'vue-router'
// ...
const openMode=ref();
const targetId=ref();
const target=ref();
const readOnly=ref(false);

onBeforeRouteUpdate(async (to, from) => {
  // react to route changes...
//   userData.value = await fetchUser(to.params.id)
    openMode.value=to.params.mode;
    if(to.params.mode==="new"){
        readOnly.value=false;
    }
    targetId.value=to.params.id;
})
</script>

<template>
    <div class="main-header">
        <!-- <Button icon="pi pi-arrow-left" severity="danger" text aria-label="Cancel">
            <span class=""></span>
            <router-link class="nav-bar-link" :to="{ name: 'targets'}">Targets</router-link>
        </Button> -->
        <router-link class="nav-bar-link" :to="{ name: 'targets'}"><i id="main-header-arrow-left" class="pi pi-arrow-left"></i></router-link>

        <div>
            <span class="title">Target</span>
            <span class="sub-title">
                {{  }}
            </span>
        </div>
        

    </div>
    <div class="main-content">
        <TabView>
            <TabPanel header="Header I">
                <p class="m-0">
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo
                    consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
                </p>
            </TabPanel>
            <TabPanel header="Header II">
                <p class="m-0">
                    Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim
                    ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Consectetur, adipisci velit, sed quia non numquam eius modi.
                </p>
            </TabPanel>
            <TabPanel header="Header III">
                <p class="m-0">
                    At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui
                    officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus.
                </p>
            </TabPanel>
        </TabView>
    </div>
</template>