import axios from "axios";

const apiRootPath = process.env.VITE_API_ROOT_PATH || "/wct";
const token = process.env.VITE_TEST_TOKEN;

axios.defaults.baseURL = `${apiRootPath}/api/v1`;

if (token) {
  axios.defaults.headers.common.Authorization = token;
}
