import type { AxiosError } from "axios";

import { apiClient } from "../../setup/apiClient";
import { NEW_TEST_PROFILE } from "./data/profiles.data";
import { NEW_TEST_TARGET, UPDATED_TEST_TARGET } from "./data/target.data";

export const post_profile = async () => {
  try {
    const response = await apiClient.post("/profiles", NEW_TEST_PROFILE);
    const profileUrl = response.data;
    const responseProfile = await apiClient.get(profileUrl);
    return responseProfile.data;
  } catch (error) {
    const axiosError = error as AxiosError;
    throw new Error(
      `Profile creation failed: ${JSON.stringify(axiosError.response?.data || axiosError.message)}`,
    );
  }
};

export const get_profile = async (profileId: number) => {
  const response = await apiClient.get(`/profiles/${profileId}`);
  return response.data;
};

export const put_profile = async (profileId: number, payload: unknown) => {
  const response = await apiClient.put(`/profiles/${profileId}`, payload);
  return response.data;
};

export const delete_profile = async (profileId: number) => {
  const response = await apiClient.delete(`/profiles/${profileId}`);
  return response.data;
};

export const get_profile_list = async () => {
  const response = await apiClient.post(`/profiles`, null, {
    headers: {
      "X-HTTP-Method-Override": "GET",
    },
  });
  return response.data;
};

export const post_target = async (
  username: string,
  authorisationId: number,
  profileId: number,
  groupId: number,
) => {
  try {
    const targetData = JSON.parse(JSON.stringify(NEW_TEST_TARGET));
    targetData.general.name = `Vest Test Target ${new Date().toISOString()}`;
    targetData.general.owner = username;
    targetData.schedule.schedules[0].owner = username;
    targetData.seeds[0].authorisations[0].id = authorisationId;
    targetData.profile.id = profileId;
    targetData.annotations.annotations[0].user = username;
    targetData.groups[0].id = groupId;

    const response = await apiClient.post("/targets", targetData);
    const targetUrl = response.headers.location;
    return targetUrl; // Return the location of the created target
  } catch (error) {
    const axiosError = error as AxiosError;
    throw new Error(
      `Target creation failed: ${JSON.stringify(axiosError.response?.data || axiosError.message)}`,
    );
  }
};

export const get_target = async (targetId: number) => {
  try {
    const response = await apiClient.get(`/targets/${targetId}`);
    return response.data;
  } catch (error) {
    const axiosError = error as AxiosError;
    throw new Error(
      `Failed to fetch target ${targetId}: ${JSON.stringify(axiosError.response?.data || axiosError.message)}`,
    );
  }
};

export const put_target = async (targetId: number) => {
  try {
    const response = await apiClient.put(
      `/targets/${targetId}`,
      UPDATED_TEST_TARGET,
    );
    return response.status;
  } catch (error) {
    const axiosError = error as AxiosError;
    throw new Error(
      `Failed to update target ${targetId}: ${JSON.stringify(axiosError.response?.data || axiosError.message)}`,
    );
  }
};

export const delete_target = async (targetId: number) => {
  try {
    const response = await apiClient.delete(`/targets/${targetId}`);
    return response.status;
  } catch (error) {
    const axiosError = error as AxiosError;
    throw new Error(
      `Failed to delete target ${targetId}: ${JSON.stringify(axiosError.response?.data || axiosError.message)}`,
    );
  }
};

export const get_target_list = async () => {
  try {
    const response = await apiClient.post(`/targets`, null, {
      headers: {
        "X-HTTP-Method-Override": "GET",
      },
    });
    return response.data;
  } catch (error) {
    const axiosError = error as AxiosError;
    throw new Error(
      `Failed to fetch target list: ${JSON.stringify(axiosError.response?.data || axiosError.message)}`,
    );
  }
};
