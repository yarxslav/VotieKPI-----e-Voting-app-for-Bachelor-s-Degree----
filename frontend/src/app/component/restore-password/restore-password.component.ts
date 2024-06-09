import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-restore-password',
  templateUrl: './restore-password.component.html',
  standalone: true,
  imports: [FormsModule, RouterModule],
  styleUrls: ['./restore-password.component.css']
})
export class RestorePasswordComponent {
  email: string = '';

  constructor(private http: HttpClient) { }

  onSubmit() {
    if (this.validateEmail(this.email)) {
      this.http.post(`${environment.baseUrl}/users/password`, { email: this.email }, 
        {responseType: 'text'})
        .subscribe(
          response => {
            alert('Запит на відновлення паролю відправлено.');
          },
          error => {
            if (error.status === 404) {
              alert('Електронну адресу не знайдено.');
            } else {
              alert('Помилка при відновленні паролю: ' + error.message);
            }
          }
        );
    } else {
      alert('Будь ласка, введіть дійсну електронну адресу.');
    }
  }

  validateEmail(email: string): boolean {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }
}
