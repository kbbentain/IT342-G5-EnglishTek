const env = import.meta.env;

// Ensure environment variables are loaded
if (!env.VITE_API_URL) {
  console.error('Missing VITE_API_URL environment variable');
}

export const config = {
  apiUrl: env.VITE_API_URL,
  appName: env.VITE_APP_NAME || 'EnglishTek Admin'
} as const;

// For debugging
console.log('Environment Config:', {
  apiUrl: config.apiUrl,
  appName: config.appName
});

export type Config = typeof config;
