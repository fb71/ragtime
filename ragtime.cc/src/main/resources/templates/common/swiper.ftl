<#--
  Media ::swiper:: as Swiper
  CSS: commons.css.ftl
  -->
<#import "/commons.ftl" as c>

<#if medias?sequence?size gt 0>
    <div id="nurEinSwiperProBeitrag" class="swiper" style="width:${size.width}px; height:${size.height}px; max-width:100%;">
      <div class="swiper-wrapper">
        <#-- Slides -->
        <#list medias?sequence as media>
            <div class="swiper-slide">
                <img src="media/${media.id}?w=${size.width}&h=${size.height}" class="d-block w-100" alt="${media.name}">
            </div>
        </#list>
      </div>
      <#-- pagination -->
      <div class="swiper-pagination"></div>
    
      <#-- navigation buttons -->
      <div class="swiper-button-prev"></div>
      <div class="swiper-button-next"></div>
    </div>

    <script type="text/javascript">
        new Swiper('#nurEinSwiperProBeitrag', {
          direction: 'horizontal',
          loop: false,
          slidesPerView: 'auto',
          spaceBetween: 5,
          autoHeight: false,
           
          pagination: {
            el: ".swiper-pagination",
          },
                   
          navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
          },
        });
    </script>

</#if>
