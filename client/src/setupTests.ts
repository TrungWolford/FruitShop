/* eslint-disable @typescript-eslint/no-unused-vars */
// Jest setup file for testing environment
import '@testing-library/jest-dom';

// Mock IntersectionObserver
global.IntersectionObserver = class IntersectionObserver {
  readonly root: Element | null = null;
  readonly rootMargin: string = '0px';
  readonly thresholds: ReadonlyArray<number> = [];
  
  constructor(_callback: IntersectionObserverCallback, _options?: IntersectionObserverInit) {}
  
  observe(_target: Element): void {}
  unobserve(_target: Element): void {}
  disconnect(): void {}
  takeRecords(): IntersectionObserverEntry[] {
    return [];
  }
} as unknown as {
  new (callback: IntersectionObserverCallback, options?: IntersectionObserverInit): IntersectionObserver;
  prototype: IntersectionObserver;
};

// Mock ResizeObserver
global.ResizeObserver = class ResizeObserver {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  constructor(_callback: ResizeObserverCallback) {}
  
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  observe(_target: Element, _options?: ResizeObserverOptions): void {}
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  unobserve(_target: Element): void {}
  disconnect(): void {}
} as unknown as {
  new (callback: ResizeObserverCallback): ResizeObserver;
  prototype: ResizeObserver;
};

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // deprecated
    removeListener: jest.fn(), // deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// Mock window.scrollTo
Object.defineProperty(window, 'scrollTo', {
  writable: true,
  value: jest.fn(),
});