import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {RouterLink, Router} from "@angular/router";
import {NgClass, CommonModule} from "@angular/common";
import {environment} from "../../../environments/environment";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  standalone: true,
  imports: [
    RouterLink,
    NgClass,
    CommonModule,
    FormsModule
  ],
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  user: any;
  currentTime: string = '';
  selectedFile: File | null = null;
  userImage: string | null = null;
  showModal: boolean = false;
  verificationStatus: string = '';
  searchQuery: string = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient
  ) {
  }

  ngOnInit(): void {
    this.getUserProfile();
    this.updateCurrentTime();
    this.loadUserImage();
  }

  getUserProfile() {
    this.user = this.authService.currentUserValue;
    this.fetchVerificationStatus();
  }

  updateCurrentTime(): void {
    const now = new Date();
    this.currentTime = now.toLocaleString('uk-UA', {
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  getVerificationStatusText(status: string): string {
    switch (status) {
      case 'NOT_VERIFIED':
        return 'Не верифіковано';
      case 'VERIFIED':
        return 'Верифіковано';
      case 'IN_PROGRESS':
        return 'В обробці';
      case 'REJECTED':
        return 'Відхилено';
      default:
        return 'Невідомий статус';
    }
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  onSubmit(event: Event) {
    event.preventDefault();
    if (this.selectedFile) {
      const reader = new FileReader();
      reader.onload = () => {
        const arrayBuffer = reader.result as ArrayBuffer;

        const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.currentUserValue.token}`);

        this.http.post(`${environment.baseUrl}/users/${this.user.id}/image`, arrayBuffer, {
          headers,
          responseType: 'arraybuffer'
        }).subscribe(
          (response: any) => {
            console.log('Файл успішно завантажено', response);
            alert('Файл успішно завантажено.');
            this.loadUserImage();
            this.fetchVerificationStatus();
          },
          (error: any) => {
            console.error('Помилка завантаження файлу', error);
            alert('Помилка завантаження файлу.');
          }
        );
      };

      reader.readAsArrayBuffer(this.selectedFile);
    } else {
      console.error('Файл не обрано');
      alert('Файл не обрано.');
    }
  }

  loadUserImage() {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.currentUserValue.token}`);
    this.http.get(`${environment.baseUrl}/users/${this.user.id}/image`, {
      headers,
      responseType: 'blob'
    }).subscribe(
      (response: Blob) => {
        if (response.size > 0) {
          const reader = new FileReader();
          reader.onload = () => {
            this.userImage = reader.result as string;
          };
          reader.readAsDataURL(response);
        } else {
          this.userImage = null;
        }
      },
      (error: any) => {
        console.error('Помилка завантаження зображення користувача', error);
        this.userImage = null;
      }
    );
  }

  confirmDelete() {
    this.showModal = true;
  }

  deleteImage() {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.currentUserValue.token}`);
    this.http.delete(`${environment.baseUrl}/users/${this.user.id}/image`, {headers})
      .subscribe(
        () => {
          console.log('Зображення успішно видалено');
          alert('Зображення успішно видалено.');
          this.userImage = null;
          this.showModal = false;
          this.fetchVerificationStatus();
        },
        (error: any) => {
          console.error('Помилка видалення зображення', error);
          alert('Помилка видалення зображення.');
          this.showModal = false;
        }
      );
  }

  fetchVerificationStatus() {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.currentUserValue.token}`);
    this.http.get(`${environment.baseUrl}/users/${this.user.id}/status`, {headers})
      .subscribe(
        (response: any) => {
          this.verificationStatus = response;
        },
        (error: any) => {
          console.error('Помилка завантаження статусу верифікації', error);
        }
      );
  }

  onSearch(): void {
    this.router.navigate(['/search-results'], { queryParams: { query: this.searchQuery } });
  }

  logout() {
    this.authService.logout();
  }
}
