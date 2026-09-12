// Authentication Service for Fasal Drishti AI Admin Portal
const ADMIN_USER = import.meta.env.VITE_ADMIN_USER || 'adminsupport';
const ADMIN_PASS = import.meta.env.VITE_ADMIN_PASSWORD || 'RupamSupport@7872';

const SESSION_KEY = 'fasal_admin_session';

export function loginAdmin(username, password, rememberMe = false) {
  const cleanUser = username?.trim();
  const cleanPass = password?.trim();

  if (cleanUser === ADMIN_USER && cleanPass === ADMIN_PASS) {
    const sessionData = {
      username: cleanUser,
      role: 'Super Administrator & Chief Agronomist',
      name: 'Rupam (Admin Support)',
      email: 'admin@fasaldrishti.ai',
      avatar: '/logo.png',
      loggedInAt: new Date().toISOString(),
      expiresAt: new Date(Date.now() + (rememberMe ? 30 * 24 * 60 * 60 * 1000 : 24 * 60 * 60 * 1000)).toISOString()
    };

    const storage = rememberMe ? localStorage : sessionStorage;
    storage.setItem(SESSION_KEY, JSON.stringify(sessionData));
    // Clear the other storage to prevent duplicate state
    if (rememberMe) {
      sessionStorage.removeItem(SESSION_KEY);
    } else {
      localStorage.removeItem(SESSION_KEY);
    }

    return { success: true, session: sessionData };
  }

  return { 
    success: false, 
    error: 'Invalid Admin credentials. Please verify your Username and Password.' 
  };
}

export function getCurrentAdminSession() {
  try {
    const raw = localStorage.getItem(SESSION_KEY) || sessionStorage.getItem(SESSION_KEY);
    if (!raw) return null;

    const session = JSON.parse(raw);
    if (new Date(session.expiresAt) < new Date()) {
      logoutAdmin();
      return null;
    }
    return session;
  } catch (_e) {
    logoutAdmin();
    return null;
  }
}

export function logoutAdmin() {
  localStorage.removeItem(SESSION_KEY);
  sessionStorage.removeItem(SESSION_KEY);
}
