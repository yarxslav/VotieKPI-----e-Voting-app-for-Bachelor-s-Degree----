import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {VotingService} from '../../services/voting.service';
import {User} from '../../models/user.model';
import {RouterLink, Router} from "@angular/router";
import {NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {Candidate} from '../../models/candidate.model';
import {Voting} from '../../models/voting.model';
import {UNIVERSITIES} from '../../constants/universities';
import {VOTING_CATEGORIES} from '../../constants/voting-categories';

@Component({
  selector: 'app-admin-create-voting',
  templateUrl: './admin-create-voting.component.html',
  styleUrls: ['./admin-create-voting.component.css'],
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    FormsModule,
    NgIf
  ]
})

export class AdminCreateVotingComponent implements OnInit {
  currentTime: string;
  user: User | null;
  userRole: string;
  voting: Voting;
  searchQuery: string = '';
  candidates: Candidate[] = [
    {
      id: null,
      name: '',
      surname: '',
      patronymic: '',
      imageName: '',
      speech: ''
    }
  ];
  selectedFiles: File[] = [];
  universities: string[] = UNIVERSITIES;
  VOTING_CATEGORIES = VOTING_CATEGORIES;

  constructor(private authService: AuthService, private votingService: VotingService, private router: Router) {
    this.currentTime = '';
    this.user = null;
    this.userRole = '';
    this.voting = {
      id: null,
      name: '',
      description: '',
      status: '',
      dateTimeFrom: '',
      dateTimeTo: '',
      category: '',
      university: '',
      group: '',
      candidates: []
    };
  }

  ngOnInit(): void {
    this.getUser();
    setInterval(() => {
      this.updateCurrentTime();
    }, 1000);
  }

  getUser(): void {
    this.authService.currentUser.subscribe((response: any) => {
      this.user = response;
    });
  }

  updateCurrentTime(): void {
    const now = new Date();
    this.currentTime = now.toLocaleString();
  }

  logout(): void {
    this.authService.logout();
  }

  addCandidate(): void {
    this.candidates.push({
      id: null,
      name: '',
      surname: '',
      patronymic: '',
      imageName: '',
      speech: ''
    });
  }

  removeCandidate(): void {
    if (this.candidates.length > 1) {
      this.candidates.pop();
    } else {
      alert('Має бути щонайменше один кандидат');
    }
  }

  onFileChange(event: any, index: number): void {
    const file = event.target.files[0];
    if (file) {
      this.candidates[index].imageName = file.name;
      if (this.selectedFiles[index]) {
        this.selectedFiles.splice(index, 1, file);
      } else {
        this.selectedFiles.push(file);
      }
    }
  }

  onTypeChange(): void {
    if (this.voting.category === VOTING_CATEGORIES.UNIVERSITY) {
      this.voting.university = '';
      this.voting.group = '';
    } else if (this.voting.category === VOTING_CATEGORIES.GROUP) {
      this.voting.group = '';
      this.voting.university = '';
    }
  }

  createVoting(): void {
    if (this.validateForm()) {
      this.voting.candidates = this.candidates;

      const formData = new FormData();
      formData.append('voting', JSON.stringify(this.voting));
      this.selectedFiles.forEach((file) => {
        formData.append('file', file);
      });

      this.votingService.createVoting(formData).subscribe(
        (response) => {
          console.log('Voting created successfully', response);
          alert('Голосування створено успішно');
        },
        (error) => {
          console.error('Error creating voting', error);
          alert('Помилка при створенні голосування');
        }
      );
    }
  }

  onSearch(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/search-adm-results'], {queryParams: {query: this.searchQuery}});
  }

  validateForm(): boolean {
    if (!this.voting.name || !this.voting.description || !this.voting.dateTimeFrom || !this.voting.dateTimeTo || !this.voting.category) {
      alert('Будь ласка, заповніть всі поля голосування.');
      return false;
    }

    if (this.voting.category === VOTING_CATEGORIES.GROUP && (!this.voting.university || !this.voting.group)) {
      alert('Будь ласка, заповніть поле групи.');
      return false;
    }

    if (this.voting.category === VOTING_CATEGORIES.UNIVERSITY && !this.voting.university) {
      alert('Будь ласка, заповніть поля університету та групи.');
      return false;
    }

    for (const candidate of this.candidates) {
      if (!candidate.name || !candidate.surname || !candidate.patronymic || !candidate.speech || !candidate.imageName) {
        alert('Будь ласка, заповніть всі поля кандидатів.');
        return false;
      }
    }

    const dateTimeFrom = new Date(this.voting.dateTimeFrom);
    const dateTimeTo = new Date(this.voting.dateTimeTo);

    if (dateTimeFrom >= dateTimeTo) {
      alert('Дата та час початку голосування не можуть бути пізніше або дорівнювати даті та часу кінця голосування.');
      return false;
    }

    return true;
  }
}
