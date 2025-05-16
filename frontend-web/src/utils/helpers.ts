/**
 * Calculates the estimated reading time for a set of content pages
 * @param content Array of markdown content strings
 * @returns Formatted reading time string (e.g., "5 min")
 */
export const calculateReadingTime = (content: string[]): string => {
  // Average reading speed (words per minute)
  const wordsPerMinute = 200;
  
  // Count total words across all content pages
  const totalWords = content.reduce((count, page) => {
    // Remove markdown syntax to get a more accurate word count
    const cleanText = page.replace(/\*\*|\*|\#|\[.*?\]\(.*?\)|\!\[.*?\]\(.*?\)|\`\`\`[\s\S]*?\`\`\`|\`.*?\`/g, '');
    const words = cleanText.trim().split(/\s+/).length;
    return count + words;
  }, 0);
  
  // Calculate minutes
  const minutes = Math.ceil(totalWords / wordsPerMinute);
  
  // Format the output
  if (minutes < 1) {
    return "< 1 min";
  } else if (minutes === 1) {
    return "1 min";
  } else {
    return `${minutes} mins`;
  }
};

/**
 * Formats a percentage value to a clean format with 1 decimal place
 * @param percentage The raw percentage value (e.g., 33.333333333333336)
 * @param decimalPlaces Number of decimal places to show (default: 1)
 * @returns Formatted percentage string (e.g., "33.3")
 */
export const formatPercentage = (percentage: number, decimalPlaces: number = 1): string => {
  // Handle edge cases
  if (percentage === 0) return '0';
  if (percentage === 100) return '100';
  
  // Round to specified decimal places
  return percentage.toFixed(decimalPlaces);
};
