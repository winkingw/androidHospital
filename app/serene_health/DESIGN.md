---
name: Serene Health
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#424752'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#727783'
  outline-variant: '#c2c6d4'
  surface-tint: '#005db6'
  primary: '#00478d'
  on-primary: '#ffffff'
  primary-container: '#005eb8'
  on-primary-container: '#c8daff'
  inverse-primary: '#a9c7ff'
  secondary: '#505f76'
  on-secondary: '#ffffff'
  secondary-container: '#d0e1fb'
  on-secondary-container: '#54647a'
  tertiary: '#42484f'
  on-tertiary: '#ffffff'
  tertiary-container: '#596067'
  on-tertiary-container: '#d4dae2'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d6e3ff'
  primary-fixed-dim: '#a9c7ff'
  on-primary-fixed: '#001b3d'
  on-primary-fixed-variant: '#00468c'
  secondary-fixed: '#d3e4fe'
  secondary-fixed-dim: '#b7c8e1'
  on-secondary-fixed: '#0b1c30'
  on-secondary-fixed-variant: '#38485d'
  tertiary-fixed: '#dde3eb'
  tertiary-fixed-dim: '#c1c7cf'
  on-tertiary-fixed: '#161c22'
  on-tertiary-fixed-variant: '#41474e'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  headline-xl:
    fontFamily: Manrope
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Manrope
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-lg:
    fontFamily: Manrope
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-md:
    fontFamily: Manrope
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Manrope
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 48px
  container-max: 1280px
---

## Brand & Style
This design system is built on a foundation of **Modern Minimalism** infused with a **Trustworthy Medical** aesthetic. The primary objective is to evoke a sense of calm, precision, and reliability for healthcare users. By prioritizing heavy whitespace and a restricted, purposeful color palette, the interface reduces cognitive load—essential for health-related decision-making.

The style leverages clean, refined typography and subtle depth to guide the user's eye, ensuring that critical health data remains the focal point. The emotional response should be one of professional reassurance, clarity, and systematic care.

## Colors
The palette is anchored by a professional **Medical Blue** (#005EB8), selected for its historical association with stability and clinical authority. 

- **Primary:** Used for key actions, active states, and brand-critical identifiers.
- **Secondary:** A muted slate blue-grey for supporting information and secondary iconography.
- **Surface/Container:** All backgrounds utilize a tiered system of ultra-light cool greys (ranging from #F8FAFC to #F1F5F9) to maintain a clinical, "sterile but warm" environment.
- **Success/Error:** Use standard semantic colors (Emerald/Rose) but desaturated slightly to match the professional tone of the blue core.

## Typography
Manrope is utilized across all levels to maintain a modern, geometric, yet highly legible appearance. 

- **Headlines:** Use tighter letter spacing and semi-bold/bold weights to establish a strong hierarchy.
- **Body:** Standardized at 16px for optimal readability. Use a slightly lighter gray (#334155) for long-form text to reduce visual jarring against the white background.
- **Labels:** Use uppercase for `label-sm` when used in navigation or small metadata tags to differentiate from body text.

## Layout & Spacing
The design system follows a **Fluid Grid** model with an 8px base unit. 

- **Desktop:** 12-column grid with 24px gutters. Content is centered within a 1280px max-width container.
- **Mobile:** 4-column grid with 16px margins. 
- **Rhythm:** Use generous vertical padding (64px+) between major sections to emphasize the minimalist, "serene" nature of the brand. Components should favor internal padding over external margins to maintain "contained" visual blocks.

## Elevation & Depth
Depth is conveyed through **Tonal Layers** and extremely soft **Ambient Shadows**. 

- **Level 0:** Main background (Primary Surface).
- **Level 1:** Content cards and containers. These use a 1px solid border (#E2E8F0) rather than a shadow to maintain a clean, clinical look.
- **Level 2:** Active/Hover states and Modals. Use a diffused shadow: `0px 10px 15px -3px rgba(0, 94, 184, 0.05)`. Note the subtle blue tint in the shadow to remain consistent with the color theme.
- **Overlays:** Use a background blur (12px) on navigation bars and modals to create a sense of spatial awareness without adding visual clutter.

## Shapes
A **Rounded** (Level 2) shape language is applied to balance the "clinical" blue with a "friendly" approachable feel. 

- **Standard Elements:** 0.5rem (8px) radius for buttons and input fields.
- **Large Elements:** 1rem (16px) radius for cards and main content containers.
- **Pills:** Use full rounding (999px) for status badges, tags, and chips to clearly distinguish them from actionable buttons.

## Components
- **Buttons:** Primary buttons use the Medical Blue background with white text. Secondary buttons use a transparent background with a 1px Blue border.
- **Input Fields:** Use a 1px slate-200 border that transitions to the Primary Blue on focus. Labels should be persistently visible above the field.
- **Cards:** White backgrounds with a subtle border (#E2E8F0). No shadow by default; apply Level 2 shadow only on hover for interactive cards.
- **Status Chips:** Use tinted backgrounds (e.g., light blue for "In Progress") with high-contrast text for accessibility.
- **Lists:** Use horizontal dividers (1px, #F1F5F9) with generous 16px vertical padding between items to ensure ease of scanning for patient records or data points.
- **Specialized Components:** Include "Health Metrics" tiles that use large typography for values and small labels for units, keeping the layout strictly aligned to the 8px grid.