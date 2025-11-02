declare module 'qrcode' {
    export interface QRCodeToDataURLOptions {
        width?: number;
        margin?: number;
        errorCorrectionLevel?: 'L' | 'M' | 'Q' | 'H';
        color?: {
            dark?: string;
            light?: string;
        };
    }

    export interface QRCodeToCanvasOptions extends QRCodeToDataURLOptions {
        scale?: number;
    }

    export function toDataURL(
        text: string,
        options?: QRCodeToDataURLOptions
    ): Promise<string>;

    export function toCanvas(
        canvas: HTMLCanvasElement,
        text: string,
        options?: QRCodeToCanvasOptions,
        callback?: (error: Error | null | undefined) => void
    ): Promise<void>;
}
