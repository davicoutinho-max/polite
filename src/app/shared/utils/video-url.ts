/** Pulls the 11-character video id out of a pasted YouTube URL (watch/short/embed/youtu.be
 * forms) so it can be handed to `ui-youtube`'s `videoId` input, which expects a bare id rather
 * than a full link. Returns null for anything else (e.g. a direct .mp4 URL), which the caller
 * treats as a plain `<video>` source instead. */
export function extractYouTubeId(url: string): string | null {
  const match = url.match(/(?:youtube\.com\/(?:watch\?v=|shorts\/|embed\/)|youtu\.be\/)([a-zA-Z0-9_-]{11})/);
  return match ? match[1] : null;
}
