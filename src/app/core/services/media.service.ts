import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

interface MediaUploadResponseDto {
  readonly url: string;
  readonly fileName: string;
}

/** Shared upload endpoint backing every image/file attachment across the platform (posts,
 * fundraising causes, petitions, profile/cover photos) — one generic "store this file, get a URL
 * back" endpoint on feed-content-service, reused rather than duplicated per feature. See
 * MediaController's javadoc on the backend. */
@Injectable({ providedIn: 'root' })
export class MediaService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = `${environment.apiBaseUrl}/api/feed`;

  upload(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<MediaUploadResponseDto>(`${this.apiBase}/media`, formData).pipe(map((r) => r.url));
  }
}
