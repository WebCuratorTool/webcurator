<template>
    <div class="nav-bar">
        <div class="nav-bar-inner">
            <img class="logo" src="@/assets/wct_logo.png" />
            <div class="nav-bar-links">
                <router-link class="nav-bar-link" to="/">Dashboard</router-link>
                <router-link class="nav-bar-link" to="/targets">Targets</router-link>
            </div>
        </div>
    </div>
</template>

<style scoped>
.nav-bar{
    position: sticky;
    top: 0;
    height: 80px;
    width: 100%;
    background-color: #1F5384;
}

.nav-bar-inner{
    height: 65%;
    margin: 0;
    position: absolute;
    top: 50%;
    -ms-transform: translateY(-50%);
    transform: translateY(-50%);
    display: flex;
}

.logo {
    height: 100%;
    padding-left: 4em;
    padding-right: 4em;
}

.nav-bar-links {
    display: flex;
    margin-top: auto;
    margin-bottom: auto;
}

.nav-bar-link {
    margin-right: 1em;
    font-size: large;
    text-decoration: none;
    color: #E4F0F5;
}
</style>