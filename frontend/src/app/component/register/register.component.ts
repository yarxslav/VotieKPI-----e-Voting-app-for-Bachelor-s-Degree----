import {Component, ViewEncapsulation} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {environment} from '../../../environments/environment';
import {HashUtil} from "../../services/hash-util.service";
import {Router, RouterModule} from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  encapsulation: ViewEncapsulation.None,
  imports: [FormsModule, RouterModule, CommonModule],
  standalone: true,
  styleUrls: ['./register.component.css']
})

export class RegisterComponent {
  username: string = '';
  email: string = '';
  password: string = '';
  confirmPassword: string = '';

  usernameError: string = '';
  emailError: string = '';
  passwordError: string = '';
  confirmPasswordError: string = '';

  private apiUrl = environment.baseUrl + '/users';

  constructor(private http: HttpClient, private router: Router) {
  }

  async onSubmit() {
    this.resetErrors();

    if (!this.username) {
      this.usernameError = 'Поле логіну не може бути порожнім';
    }
    if (!this.email) {
      this.emailError = 'Поле електронної пошти не може бути порожнім';
    } else if (!this.validateEmail(this.email)) {
      this.emailError = 'Некоректна електронна пошта';
    }
    if (!this.password) {
      this.passwordError = 'Поле паролю не може бути порожнім';
    }
    if (!this.confirmPassword) {
      this.confirmPasswordError = 'Поле підтвердження паролю не може бути порожнім';
    }
    if (this.password !== this.confirmPassword) {
      this.confirmPasswordError = 'Паролі не співпадають';
    }

    if (this.usernameError || this.emailError || this.passwordError || this.confirmPasswordError) {
      return;
    }

    const hashedPassword = await HashUtil.hashPassword(this.password);

    const registrationData = {
      username: this.username,
      email: this.email,
      password: hashedPassword
    };

    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    this.http.post(this.apiUrl, registrationData, {headers}).subscribe(
      response => {
        alert('Реєстрація успішна');
        this.router.navigate(['/login']);
      },
      error => {
        alert('Помилка при реєстрації: ' + error.message);
      }
    );
  }

  resetErrors() {
    this.usernameError = '';
    this.emailError = '';
    this.passwordError = '';
    this.confirmPasswordError = '';
  }

  validateEmail(email: string) {
    const re = /^([a-zA-Z0-9_\-\.]+@[a-zA-Z0-9\-.]+\.[a-zA-Z]{2,}$)/;
    return re.test(String(email).toLowerCase());
  }
}
