import {
  GoogleGenAI,
} from '@google/genai';

const ai = new GoogleGenAI({
  apiKey: import.meta.env.VITE_GEMINI_API_KEY,
});

export interface Message {
  role: 'user' | 'model'
  text: string,
}


export async function chatMessageAi(history: Message[]) {
  const config = {
    systemInstruction: `Bạn là nhân viên tư vấn nhiệt tình và duyên dáng của cửa hàng FruitShop. 
    Nhiệm vụ của bạn là:
    - Chào hỏi thân thiện.
    - Tư vấn các loại trái cây (táo, cam, nho, dưa hấu...) tươi ngon, nguồn gốc rõ ràng.
    - Gợi ý trái cây theo mùa hoặc theo nhu cầu (giảm cân, biếu tặng).
    - Luôn trả lời ngắn gọn, lịch sự, dùng emoji phù hợp`
  };
  const model = 'gemini-2.5-flash-lite';
  const contents = history.map(msg => ({
    role: msg.role,
    parts: [{ text: msg.text }]
  }));

  let fullText = '';
  const response = await ai.models.generateContentStream({
    model,
    config,
    contents
  });
  for await (const chunk of response) {
    console.log('Chunk:', chunk.text);
    fullText += chunk.text;
  }
  return fullText;
}


