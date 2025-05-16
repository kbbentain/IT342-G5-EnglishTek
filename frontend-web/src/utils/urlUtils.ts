export const BACKEND_URL = import.meta.env.VITE_API_URL || 'https://api-englishtek.aetherrflare.org';

export const getFullImageUrl = (iconUrl: string | null | undefined): string | null => {
  if (!iconUrl) return null;
  
  // If it's already a full URL, return as is
  if (iconUrl.startsWith('http')) return iconUrl;
  
  // For API paths like /api/v1/files/..., we need to use the full URL
  return `${BACKEND_URL}${iconUrl}`;
};
