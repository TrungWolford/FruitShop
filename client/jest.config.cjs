module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>/src'],
  
  // Cấu hình ts-jest sử dụng tsconfig.app.json
  globals: {
    'ts-jest': {
      tsconfig: 'tsconfig.app.json'
    }
  },
  
  // Chỉ chạy test cho file có .test hoặc .spec
  testMatch: [
    '**/__tests__/**/*.+(ts|tsx)',
    '**/?(*.)+(spec|test).+(ts|tsx)'
  ],
  
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json', 'node'],
  
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
  },
  
  // Bỏ qua các file/thư mục này khi tìm test
  testPathIgnorePatterns: [
    '/node_modules/',
    '/dist/',
    '/build/',
  ],
  
  // CHỈ collect coverage từ file được import trong test
  collectCoverage: true,
  collectCoverageFrom: [
    'src/components/tessJest/**/*.{ts,tsx}',  // CHỈ folder tessJest
    '!src/**/*.d.ts',
    '!src/**/*.test.{ts,tsx}',
    '!src/**/*.spec.{ts,tsx}',
  ],
};