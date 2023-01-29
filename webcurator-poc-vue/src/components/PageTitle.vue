<template>
  <div class="page-title">
    <i>
      <slot name="title"></slot>
    </i>
  </div>
</template>

<style scoped>
.page-title {
  /* position: relative; */
  height: 3em;
  background: #F8F9FA;
  /* background: #6582ff; */
  color: #1F5384;
  font-size: x-large;
  text-align: left;
  margin: 0;
  padding: 0;
  display: flex;
  /* justify-content: center; */
  align-items: center;

}
/* .page-title i{
  margin-left: 2px;
  position: absolute;
  top: 50%;
  -ms-transform: translateY(-50%);
  transform: translateY(-50%);
} */
</style>
  