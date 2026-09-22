import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-engineer-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './engineer-register.component.html',
  styleUrl: './engineer-register.component.scss',
})
export class EngineerRegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);

  readonly isSubmitting = signal(false);
  readonly isSubmitted = signal(false);
  readonly showPassword = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly specializationOptions: string[] = [
    'Routing & Switching',
    'Firewall & Security',
    'Cloud Networking',
    'Wireless & Mobility',
    'Data Center Infrastructure',
    'Network Automation & DevOps',
    'VoIP & Unified Communications',
  ];

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    specialization: ['Routing & Switching', [Validators.required]],
  });

  togglePasswordVisibility(): void {
    this.showPassword.update((v) => !v);
  }

  submit(): void {
    this.errorMessage.set(null);

    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const payload = this.form.getRawValue();
    console.log('[EngineerRegisterComponent] Submitting payload:', payload);

    this.auth.registerEngineer(payload).subscribe({
      next: (res) => {
        console.log('[EngineerRegisterComponent] Registration Success:', res);
        this.isSubmitting.set(false);
        this.isSubmitted.set(true);
        this.notifications.success('Engineer account created successfully! You can now log in.');
      },
      error: (err) => {
        console.error('[EngineerRegisterComponent] Registration Error:', err);
        this.isSubmitting.set(false);
        const msg = err.error?.message ?? err.message ?? 'Registration failed. Please check your details and try again.';
        this.errorMessage.set(msg);
      },
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
