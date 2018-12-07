import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Subscription} from "rxjs";
import {RequestsService} from "../../core/services/requests.service";
import {Request} from "../../core/models/Request";

@Component({
  selector: 'app-request-detail',
  templateUrl: './request-detail.component.html',
  styleUrls: ['./request-detail.component.scss']
})
export class RequestDetailComponent implements OnInit, OnDestroy {

  constructor(
    private route: ActivatedRoute,
    private requestsService: RequestsService
  ) { }

  private sub : Subscription;

  loading = true;
  request: Request;

  ngOnInit() {
    this.sub = this.route.params.subscribe(params => {
      this.requestsService.getRequest(params['id']).subscribe(request => {
        this.request = request;
        this.loading = false;
      });
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
