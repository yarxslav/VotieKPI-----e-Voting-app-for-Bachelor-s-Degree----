import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminCreateVotingComponent } from './admin-create-voting.component';

describe('AdminCreateVotingComponent', () => {
  let component: AdminCreateVotingComponent;
  let fixture: ComponentFixture<AdminCreateVotingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCreateVotingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminCreateVotingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
