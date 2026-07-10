import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface AreaPoint {
  readonly label: string;
  readonly value: number;
}

const W = 640;
const H = 220;
const PAD_X = 16;
const PAD_TOP = 16;
const PAD_BOTTOM = 28;

/**
 * Generic single-series area/line chart (change over time). One sequential hue,
 * 2px non-scaling line, recessive gridlines, ≥8px hover markers with a native
 * tooltip. Text uses ink tokens.
 *
 * @example <ui-area-chart [points]="weekly" suffix="%" />
 */
@Component({
  selector: 'ui-area-chart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg class="chart" [attr.viewBox]="'0 0 ' + W + ' ' + H" role="img">
      <!-- recessive gridlines -->
      @for (g of gridlines(); track g) {
        <line class="grid" [attr.x1]="padX" [attr.x2]="W - padX" [attr.y1]="g" [attr.y2]="g" />
      }
      <!-- area + line -->
      <path class="area" [attr.d]="areaPath()" />
      <path class="line" [attr.d]="linePath()" vector-effect="non-scaling-stroke" />
      <!-- markers -->
      @for (p of geometry(); track p.label) {
        <circle class="dot" [attr.cx]="p.x" [attr.cy]="p.y" r="4">
          <title>{{ p.label }}: {{ p.value }}{{ suffix() }}</title>
        </circle>
      }
    </svg>
    <div class="labels">
      @for (p of points(); track p.label) {
        <span>{{ p.label }}</span>
      }
    </div>
  `,
  styles: `
    :host { display: block; }
    .chart { width: 100%; height: auto; display: block; overflow: visible; }
    .grid { stroke: var(--cp-outline-variant); stroke-width: 1; opacity: 0.5; }
    .area { fill: var(--cp-secondary); opacity: 0.12; }
    .line { fill: none; stroke: var(--cp-secondary); stroke-width: 2; stroke-linejoin: round; stroke-linecap: round; }
    .dot { fill: var(--cp-secondary); stroke: var(--cp-surface-container-lowest); stroke-width: 2; }
    .labels {
      display: flex; justify-content: space-between; margin-top: var(--cp-space-xs);
      font-size: 12px; color: var(--cp-on-surface-variant);
    }
  `,
})
export class UiAreaChart {
  readonly points = input.required<readonly AreaPoint[]>();
  readonly suffix = input('');

  protected readonly W = W;
  protected readonly H = H;
  protected readonly padX = PAD_X;

  protected readonly geometry = computed(() => {
    const pts = this.points();
    const max = Math.max(1, ...pts.map((p) => p.value));
    const plotW = W - PAD_X * 2;
    const plotH = H - PAD_TOP - PAD_BOTTOM;
    const step = pts.length > 1 ? plotW / (pts.length - 1) : 0;
    return pts.map((p, i) => ({
      label: p.label,
      value: p.value,
      x: PAD_X + step * i,
      y: PAD_TOP + plotH - (p.value / max) * plotH,
    }));
  });

  protected readonly linePath = computed(() =>
    this.geometry()
      .map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`)
      .join(' '),
  );

  protected readonly areaPath = computed(() => {
    const g = this.geometry();
    if (!g.length) {
      return '';
    }
    const baseline = H - PAD_BOTTOM;
    const line = g.map((p) => `L${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ');
    const lastX = g.at(-1)!.x.toFixed(1);
    return `M${g[0].x.toFixed(1)},${baseline} ${line} L${lastX},${baseline} Z`;
  });

  protected readonly gridlines = computed(() => {
    const plotH = H - PAD_TOP - PAD_BOTTOM;
    return [0, 0.25, 0.5, 0.75, 1].map((f) => PAD_TOP + plotH * f);
  });
}
