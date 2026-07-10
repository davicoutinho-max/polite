import { ChangeDetectionStrategy, Component, effect, input, signal } from '@angular/core';
import * as QRCode from 'qrcode';

/**
 * Generic QR code. Renders `data` to a PNG data-URL via the `qrcode` library.
 *
 * @example <ui-qrcode data="https://…" [size]="160" />
 */
@Component({
  selector: 'ui-qrcode',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (src(); as url) {
      <img class="qr" [src]="url" [width]="size()" [height]="size()" alt="QR code" />
    } @else {
      <div class="qr qr--placeholder" [style.width.px]="size()" [style.height.px]="size()"></div>
    }
  `,
  styles: `
    :host { display: inline-block; line-height: 0; }
    .qr { border-radius: var(--cp-radius); display: block; }
    .qr--placeholder { background: var(--cp-surface-container-high); border-radius: var(--cp-radius); }
  `,
})
export class UiQrcode {
  readonly data = input.required<string>();
  readonly size = input(160);

  protected readonly src = signal<string | null>(null);

  constructor() {
    effect(() => {
      const payload = this.data();
      const size = this.size();
      QRCode.toDataURL(payload, {
        width: size,
        margin: 1,
        color: { dark: '#002045', light: '#ffffff' },
      })
        .then((url) => this.src.set(url))
        .catch(() => this.src.set(null));
    });
  }
}
