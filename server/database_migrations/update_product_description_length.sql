-- Migration: Increase description column length from 255 to 3600 characters
-- Date: 2025-12-16
-- Description: Update products table to allow longer product descriptions

-- Increase the length of the description column
ALTER TABLE products 
ALTER COLUMN description TYPE VARCHAR(3600);

-- Optional: Add a comment to document the change
COMMENT ON COLUMN products.description IS 'Product description - Maximum 3600 characters';
