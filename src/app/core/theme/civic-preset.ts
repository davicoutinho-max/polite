import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

/**
 * CivicPulse PrimeNG preset.
 *
 * Derived from Aura and re-tuned to the institutional blue palette defined in
 * the design system. The ramp anchors on the deep navy `--cp-primary` (#002045)
 * and brightens toward the interactive secondary blue (#1960a3) used for
 * buttons, links and focus rings.
 */
export const CivicPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#eff4ff',
      100: '#d6e3ff',
      200: '#adc7f7',
      300: '#7db6ff',
      400: '#4a91e0',
      500: '#1960a3',
      600: '#14538f',
      700: '#1a365d',
      800: '#10294a',
      900: '#002045',
      950: '#001b3c',
    },
    colorScheme: {
      light: {
        primary: {
          color: '#1960a3',
          contrastColor: '#ffffff',
          hoverColor: '#14538f',
          activeColor: '#10294a',
        },
        highlight: {
          background: '#7db6ff',
          focusBackground: '#adc7f7',
          color: '#00477f',
          focusColor: '#00477f',
        },
        surface: {
          0: '#ffffff',
          50: '#f8f9ff',
          100: '#eff4ff',
          200: '#e5eeff',
          300: '#dce9ff',
          400: '#d3e4fe',
          500: '#c4c6cf',
          600: '#a7abb5',
          700: '#74777f',
          800: '#43474e',
          900: '#213145',
          950: '#0b1c30',
        },
        formField: {
          background: '#ffffff',
          borderColor: '#c4c6cf',
          hoverBorderColor: '#74777f',
          focusBorderColor: '#1960a3',
          color: '#0b1c30',
          placeholderColor: '#74777f',
        },
        content: {
          background: '#ffffff',
          borderColor: '#c4c6cf',
        },
        text: {
          color: '#0b1c30',
          mutedColor: '#43474e',
        },
      },
    },
  },
});
