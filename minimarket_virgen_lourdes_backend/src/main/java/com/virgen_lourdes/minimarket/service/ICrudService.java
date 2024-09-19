package com.virgen_lourdes.minimarket.service;

import java.util.List;

public interface ICrudService<RequestDto, ResponseDto, I> {

    ResponseDto create(RequestDto requestDto);
    List<ResponseDto> read(RequestDto requestDto);
    ResponseDto update(RequestDto requestDto, Long id);
    void delete(Long id);
    void deactivateUser(Long id);
}
