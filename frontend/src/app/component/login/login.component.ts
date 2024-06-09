import {Component} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {HttpErrorResponse} from '@angular/common/http';
import {HashUtil} from "../../services/hash-util.service";
import {FormsModule} from "@angular/forms";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink
  ],
  styleUrls: ['./login.component.css']
})

export class LoginComponent {
  username: string = '';
  password: string = '';

  constructor(private authService: AuthService) {
  }

  async onSubmit() {
    try {
      const hashedPassword = await HashUtil.hashPassword(this.password);
      this.authService.login(this.username, hashedPassword).subscribe(
        (response) => {
          console.log('Успешный вход:', response);
        },
        (error) => {
          if ((error as HttpErrorResponse).status === 401) {
            alert('Невірний логін або пароль!');
          } else {
            alert('Помилка під час входу: ' + error.message);
          }
        }
      );
    } catch (error) {
      console.error('Password hashing error:', error);
    }
  }
}
