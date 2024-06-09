import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminHomeUsersComponent } from './admin-home-users.component';

describe('AdminHomeUsersComponent', () => {
  let component: AdminHomeUsersComponent;
  let fixture: ComponentFixture<AdminHomeUsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHomeUsersComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminHomeUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
