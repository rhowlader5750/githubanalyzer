import axios from 'axios';



const baseURL =  'http://localhost:8080/api/github';

export const getallRepo = (username) => {
  return axios.get(`${baseURL}/allrepos`, {
    params: { owner: username }
  }).then(res => res.data); 
};

export const getUserInfo = (username) => {
  return axios.get(`${baseURL}/userinfo`, {
    params: { owner: username }
  }).then(res => res.data); 
};

export const getReadMe = (username, repoName) =>{
  return axios.get(`${baseURL}/repo/overview`, {
    params: { owner: username, repo:repoName}
  }).then(res => res.data);
} 

export const saveRepo = (repoName) =>{
  return axios.post(`${baseURL}/usersavedrepos/save`, repoName);
  }

export const deleteRepo = (id) => {
  return axios.delete(`${baseURL}/usersavedrepos/${id}`);
};

export const fetchSavedRepos = (username) => {
  return axios.get(`${baseURL}/usersavedrepos`, {
    params: { username: username }
  }).then(res => res.data);
}

export const getProfileReadMe = (username) => {
  return axios.get(`${baseURL}/repos/profileoverview`, {
    params: {username:username}
  }).then(res => res.data);
  
}
