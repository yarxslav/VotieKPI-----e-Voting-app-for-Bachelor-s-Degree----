import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {User} from '../../models/user.model';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {RouterLink, Router} from "@angular/router";
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {environment} from '../../../environments/environment';
import {Voting} from '../../models/voting.model';
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-admin-home-votings',
  templateUrl: './admin-home-votings.component.html',
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    NgIf,
    NgClass,
    FormsModule
  ],
  styleUrls: ['./admin-home-votings.component.css']
})

export class AdminHomeVotingsComponent implements OnInit {
  currentTime: string;
  user: User | null;
  userRole: string;
  searchQuery: string = '';
  votings: Voting[] = [];
  filteredVotings: Voting[] = [];
  selectedStatus: string = 'ALL';
  selectedType: string = 'ALL';

  constructor(private authService: AuthService, private http: HttpClient, private router: Router) {
    this.currentTime = '';
    this.user = null;
    this.userRole = '';
  }

  ngOnInit(): void {
    this.updateCurrentTime();
    setInterval(() => this.updateCurrentTime(), 60000);

    this.authService.currentUser.subscribe((user: User) => {
      this.user = user;
      this.userRole = user?.roles[0]?.name || '';
    });

    this.getVotings();
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

  getVotings(): void {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    this.http.get<Voting[]>(environment.baseUrl + '/votings', {headers}).subscribe(
      (data) => {
        this.votings = data.map(voting => ({
          ...voting,
          dateTimeFrom: this.formatDateTime(voting.dateTimeFrom),
          dateTimeTo: this.formatDateTime(voting.dateTimeTo)
        }));
        this.filterVotings();
      },
      (error) => {
        console.error('Error fetching votings', error);
      }
    );
  }

  formatDateTime(dateTime: string): string {
    const date = new Date(dateTime);
    return date.toLocaleString('uk-UA', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  logout(): void {
    this.authService.logout();
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'active';
      case 'COMPLETED':
        return 'finished';
      case 'SUSPENDED':
        return 'suspended';
      case 'SCHEDULED':
        return 'scheduled';
      default:
        return '';
    }
  }

  getStatusTranslation(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Активне';
      case 'COMPLETED':
        return 'Завершене';
      case 'SUSPENDED':
        return 'Призупинено';
      case 'SCHEDULED':
        return 'Планується';
      default:
        return status;
    }
  }

  getImageSrc(type: string): string {
    return type === 'Групове' ? 'assets/group.png' : 'assets/university.png';
  }

  getVotingTitle(voting: Voting): string {
    if (voting.category === 'Групове') {
      return `${voting.group} | ${voting.name}`;
    }
    return voting.name;
  }

  onTypeChange(type: string): void {
    this.selectedType = type;
    this.filterVotings();
  }

  onSearch(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/search-adm-results'], { queryParams: { query: this.searchQuery } });
  }

  filterVotings(event?: Event): void {
    if (event) {
      const target = event.target as HTMLSelectElement;
      this.selectedStatus = target.value;
    }

    let filteredByStatus = this.selectedStatus === 'ALL' ? this.votings : this.votings.filter(voting => voting.status === this.selectedStatus);
    this.filteredVotings = this.selectedType === 'ALL' ? filteredByStatus : filteredByStatus.filter(voting => voting.category === this.selectedType);
  }
}
