/*
 * Copyright (C) 2024, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package ragtime.cc.article;

import java.util.ArrayList;
import java.util.Arrays;
import org.polymap.model2.query.Query.Order;

import areca.common.Platform;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.component2.TextField;
import areca.ui.component2.TextField.Type;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.form.Form;
import ragtime.cc.UICommon;
import ragtime.cc.model.Article;
import ragtime.cc.model.MediaEntity;
import ragtime.cc.website.http.MediaContentProvider;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ArticlePage {

    private static final Log LOG = LogFactory.getLog( ArticlePage.class );

    public static final ClassInfo<ArticlePage> INFO = ArticlePageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    @Page.Context
    protected ArticleEditState  state;

    protected Action            submitBtn;

    private Form                form;


    @Page.CreateUI
    public UIComponent createUI( UIComposite parent ) {
        ui.init( parent ).title.set( state.article.$().title.get() );

        form = new Form();
        form.subscribe( ev -> {
            LOG.info( "updateEnabled(): changed = %s, valid = %s", form.isChanged(), form.isValid() );
            boolean enabled = form.isChanged() && form.isValid();
            submitBtn.icon.set( enabled ? "done" : "" );
            submitBtn.enabled.set( enabled );
        });

        ui.body.layout.set( uic.verticalL().fillHeight( true ) );

        ui.body.add( form.newField().label( "Titel" )
                .model( new PropertyModel<>( state.article.$().title ) )
                .viewer( new TextFieldViewer() )
                .create()
                .layoutConstraints.set( RowConstraints.height( 35 ) ) );

        ui.body.add( form.newField()
                .model( new PropertyModel<>( state.article.$().content ) )
                .viewer( new TextFieldViewer().configure( (TextField t) -> {
                    t.multiline.set( true );
                    t.type.set( Type.MARKDOWN );
                    autocompletes( t );
                }))
                .create()
                .layoutConstraints.set( null ) ); //RowConstraints.height( 300 ) ) );

//        ui.body.add( new Text() {{
//            content.set( "Angelegt: " + state.article.$().created.get() );
//            layoutConstraints.set( RowConstraints.height( 15 ) );
//        }});
//
//        ui.body.add( new Text() {{
//            content.set( "Geändert: " + state.article.$().created.get() );
//            enabled.set( false );
//            layoutConstraints.set( RowConstraints.height( 15 ) );
//        }});

        form.load();

        // action: submit
        site.actions.add( submitBtn = new Action() {{
            //icon.set( "done" );
            description.set( "Speichern" );
            //type.set( Button.Type.SUBMIT );
            enabled.set( false );
            handler.set( ev -> {
                form.submit();
                state.submitAction().onSuccess( __ -> {
                    submitBtn.enabled.set( false );
                });
            });
        }});
        return ui;
    }


    protected void autocompletes( TextField t ) {
        var result = new ArrayList<String>();
        Platform.schedule( 2000, () -> null )
                .then( __ -> {
                    return state.uow.query( Article.class )
                            .orderBy( Article.TYPE.title, Order.ASC )
                            .executeCollect();
                })
                .then( rs -> {
                    rs.forEach( article -> result.add( article.title.get() ) );
                    result.add( "----" );
                    return state.uow.query( MediaEntity.class ).orderBy( MediaEntity.TYPE.name, Order.ASC ).executeCollect();
                })
                .onSuccess( rs -> {
                    rs.forEach( media -> result.add( MediaContentProvider.PATH + "/" + media.name.get() ) );
                    result.add( "----" );

                    result.addAll( Arrays.asList( "article", "home", "frontpage?n=", "frontpage?t=") );

                    LOG.info( "autocomplete: %s", result );
                    t.autocomplete.set( result );
                });
    }

}
