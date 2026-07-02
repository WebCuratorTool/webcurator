import axios from "axios";

const apiRootPath =
  process.env.VITE_API_ROOT_PATH || process.env.VITE_API_ROOT_PATH || "/wct";

export const apiClient = axios.create({
  baseURL: `${apiRootPath}/api/v1`,
  timeout: 60000,
});

apiClient.interceptors.request.use((config) => {
  const token = process.env.VITE_TEST_TOKEN;

  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = token;
  }

  console.log("config.headers.Authorization: " + config.headers.Authorization);

  return config;
});
