// src/components/ui/Button/Button.tsx
import * as React from 'react';
import { Slot } from '@radix-ui/react-slot';
import { type VariantProps } from 'class-variance-authority';

import { cn } from '@/libs/utils';
import { ButtonVariants } from './ButtonVariants'; // Import từ file riêng

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof ButtonVariants> {
  asChild?: boolean;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, hover, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : 'button';
    return <Comp className={cn(ButtonVariants({ variant, size, hover, className }))} ref={ref} {...props} />;
  },
);
Button.displayName = 'Button';

export { Button }; // Chỉ export Button
