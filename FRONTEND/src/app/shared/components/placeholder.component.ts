import { Component, Input } from '@angular/core';

/**
 * Stand-in page used only until the real feature module for a route is
 * built (Segments 1-8). Lets routing/guards be wired and tested end to
 * end before any real UI exists.
 */
@Component({
  selector: 'app-placeholder',
  standalone: true,
  template: `
    <div class="placeholder">
      <h2>{{ title }}</h2>
      <p>This screen hasn't been built yet — coming in a later segment.</p>
    </div>
  `,
  styles: [
    `
      .placeholder {
        padding: 2rem;
        text-align: center;
        color: var(--color-text-muted);
      }
    `,
  ],
})
export class PlaceholderComponent {
  @Input() title = 'Coming soon';
}
