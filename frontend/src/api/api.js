const API_URL = "/api/v1";

export function getToken() {
  return localStorage.getItem("token");
}

export function saveToken(token) {
  localStorage.setItem("token", token);
}

export function removeToken() {
  localStorage.removeItem("token");
}

export function logout() {
  removeToken();
}

export function getCurrentUser() {
  const token = getToken();
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return {
      id: payload.userId || payload.id,
      login: payload.sub,
      roles: payload.roles || [],
    };
  } catch {
    return null;
  }
}

async function request(path, options = {}) {
  const token = getToken();

  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  try {
    const response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers,
    });

    let data = null;
    const text = await response.text();

    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        data = text;
      }
    }

    if (!response.ok) {
      if (response.status === 401) {
        removeToken();
      }

      const errorMessage =
        (data && data.message) ||
        (typeof data === "string" && data) ||
        `Ошибка запроса: ${response.status}`;

      const error = new Error(errorMessage);
      error.status = response.status;
      throw error;
    }

    return data;
  } catch (error) {
    console.error('API request failed:', error);
    throw error;
  }
}

export async function login(login, password) {
  const data = await request("/auth/login", {
    method: "POST",
    body: JSON.stringify({ login, password }),
  });

  if (data?.token) {
    saveToken(data.token);
  }

  return data;
}

export async function register(userData) {
  const data = await request("/auth/register", {
    method: "POST",
    body: JSON.stringify(userData),
  });
  return data;
}

export async function getProfile() {
  return request("/profile");
}

export async function getAllInitiatives() {
  return request("/initiatives");
}

export async function getMyInitiatives() {
  return request("/initiatives/my");
}

export async function getInitiativeById(id) {
  return request(`/initiatives/${id}`);
}

export async function createInitiative(initiativeData) {
  return request("/initiatives", {
    method: "POST",
    body: JSON.stringify(initiativeData),
  });
}

export async function updateInitiative(id, initiativeData) {
  return request(`/initiatives/${id}`, {
    method: "PUT",
    body: JSON.stringify(initiativeData),
  });
}

export async function deleteInitiative(id) {
  return request(`/initiatives/${id}`, {
    method: "DELETE",
  });
}

export async function getAllApplications() {
  return request("/applications");
}

export async function updateApplicationStatus(id, status) {
  return request(`/applications/${id}/status/${status}`, {
    method: "PUT",
  });
}

export async function createApplication(applicationData) {
  return request("/applications", {
    method: "POST",
    body: JSON.stringify(applicationData),
  });
}

export async function getMyApplications() {
  return request("/applications/my");
}

export async function getApplicationById(id) {
  return request(`/applications/${id}`);
}

export async function resubmitInitiative(id, initiativeData) {
    return request(`/initiatives/${id}/resubmit`, {
        method: "PUT",
        body: JSON.stringify(initiativeData),
    });
}