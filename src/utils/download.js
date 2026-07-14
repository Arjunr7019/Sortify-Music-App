import * as FileSystem from "expo-file-system";

const DIR = FileSystem.documentDirectory + "sortify-downloads/";

async function ensureDir() {
  const info = await FileSystem.getInfoAsync(DIR);
  if (!info.exists) {
    await FileSystem.makeDirectoryAsync(DIR, { intermediates: true });
  }
}

export async function downloadSongFile(song) {
  await ensureDir();
  const ext = song.audioUrl?.split("?")[0].split(".").pop() || "mp3";
  const localUri = `${DIR}${song.id}.${ext}`;
  const result = await FileSystem.downloadAsync(song.audioUrl, localUri);
  return result.uri;
}

export async function deleteSongFile(localUri) {
  try {
    await FileSystem.deleteAsync(localUri, { idempotent: true });
  } catch (e) {
    // ignore
  }
}
