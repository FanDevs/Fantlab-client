package org.odddev.fantlab.auth.reg;

import org.odddev.fantlab.auth.IAuthProvider;
import org.odddev.fantlab.core.di.Injector;
import org.odddev.fantlab.core.layers.presenter.Presenter;

import javax.inject.Inject;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * @author kenrube
 * @since 20.09.16
 */

public class RegPresenter extends Presenter<IRegView> {

    private Subscription mRegSubscription;

    @Inject
    CompositeSubscription mCompositeSubscription;

    @Inject
    IAuthProvider mAuthProvider;

    public RegPresenter() {
        Injector.getAppComponent().inject(this);
    }

    public void register(RegParams params) {
        mRegSubscription = mAuthProvider
                .register(
                        params.username,
                        params.password,
                        params.email)
                .subscribe(
                        this::showResult,
                        throwable -> showError(throwable.getLocalizedMessage()),
                        () -> mCompositeSubscription.remove(mRegSubscription));
        mCompositeSubscription.add(mRegSubscription);
    }

    private void showResult(boolean hasCookie) {
        for (IRegView view : getViews()) {
            view.showRegResult(hasCookie);
        }
    }

    private void showError(String error) {
        for (IRegView view : getViews()) {
            view.showError(error);
        }
    }

    @Override
    protected void onDestroy() {
        mCompositeSubscription.unsubscribe();
        super.onDestroy();
    }
}
