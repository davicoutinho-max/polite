import { Permission } from './permission.model';

export interface NavItem {
  /** English fallback, shown until a translation-tag value exists for `key`. */
  readonly label: string;
  /** Translation-tag key (e.g. `nav.feed`) resolved against the current language. */
  readonly key: string;
  /** Material Symbols icon name. */
  readonly icon: string;
  readonly route: string;
  /** Shown in the compact mobile bottom bar. */
  readonly mobile?: boolean;
  /** When set, the item only appears for accounts holding this permission — or, if given an
   * array, ANY one of them. */
  readonly permission?: Permission | Permission[];
}
